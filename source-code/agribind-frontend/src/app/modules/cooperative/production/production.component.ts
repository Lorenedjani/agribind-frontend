// src/app/modules/cooperative/production/production.component.ts

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViewEncapsulation } from '@angular/core';
import { CooperativeSidebarComponent } from "../../../../shared/cooperative-sidebar/cooperative-sidebar.component";
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { MockProductionService } from '../../../core/services/production.service.mock';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { Subject, of, Observable } from 'rxjs';

interface ProductionRecord {
  productionId: string;
  farmerId: string;
  farmerName: string;
  cropType: string;
  quantity: number;
  qualityGrade: string;
  warehouse: string;
  deliveryDate: string;
  valueXaf: number;
  status: string;
}

interface FarmerSearchResult {
  userId: string;
  name: string;
  phoneNumber: string;
  agriculturalType: string;
  cropTypes: string[];
  landArea: number;
}

interface DashboardMetrics {
  totalProduction: string;
  totalProductionPercent: string;
  activeFarmers: number;
  activeFarmersParticipation: string;
  gradeAProduction: string;
  gradeAPercent: string;
  thisMonthDeliveries: string;
  thisMonthChange: string;
}

interface CreateProductionRequest {
  farmerId: string;
  cropType: string;
  quantity: number;
  qualityGrade: string;
  warehouse: string;
  deliveryDate: string;
}

interface ApiUserResponse {
  content: any[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, FormsModule, CooperativeSidebarComponent],
  templateUrl: './production.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./production.component.scss']
})
export class ProductionComponent implements OnInit {
  // User info from auth service
  user = {
    name: '',
    role: '',
    initials: '',
    cooperativeId: ''
  };

  // Dashboard metrics
  totalProduction = '0 MT';
  totalProductionPercent = '0%';
  activeFarmers = 0;
  activeFarmersParticipation = '0%';
  gradeAProduction = '0 MT';
  gradeAPercent = '0%';
  thisMonthDeliveries = '0 MT';
  thisMonthChange = '0%';

  // Filter options
  searchQuery = '';
  selectedCrop = 'All Crops';
  selectedGrade = 'All Grades';
  cropTypes: string[] = ['All Crops', 'COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON'];
  qualityGrades: string[] = ['All Grades', 'GRADE_A', 'GRADE_B', 'GRADE_C'];
  warehouses: string[] = [];

  // Production data
  productionRecords: ProductionRecord[] = [];
  filteredProduction: any[] = [];
  paginatedProduction: any[] = [];

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;

  // Modals
  showRecordModal = false;
  showExportModal = false;

  // New production form
  newProduction: CreateProductionRequest = {
    farmerId: '',
    cropType: 'COCOA',
    quantity: 0,
    qualityGrade: 'GRADE_A',
    warehouse: '',
    deliveryDate: new Date().toISOString().split('T')[0]
  };

  // Farmer search
  farmerSearchTerm = '';
  searchedFarmers: FarmerSearchResult[] = [];
  selectedFarmer: FarmerSearchResult | null = null;
  showFarmerDropdown = false;
  private farmerSearchSubject = new Subject<string>();

  // Export options
  exportFormat = 'csv';
  exportDateRange = 'all';
  exportStartDate = '';
  exportEndDate = '';

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private productionService: MockProductionService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.setupFarmerSearch();
    this.loadWarehouses();
  }

  loadCurrentUser(): void {
    const currentUser = this.authService.getCurrentUser();

    if (currentUser) {
      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        cooperativeId: currentUser.cooperativeId || ''
      };

      // Load dashboard data after getting user info
      this.loadDashboardMetrics();
      this.loadProductions();
    } else {
      console.error('No user logged in');
    }
  }

  formatRole(role: string): string {
    const roleMap: { [key: string]: string } = {
      'COOPERATIVE': 'Cooperative Manager',
      'FARMER': 'Farmer',
      'GOVERNMENT': 'Government Official'
    };
    return roleMap[role] || role;
  }

  getInitials(name: string): string {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  setupFarmerSearch(): void {
    this.farmerSearchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((term: string) => {
        if (term.length < 2) {
          return of([] as FarmerSearchResult[]);
        }
        return this.searchFarmers(term).pipe(
          catchError(() => of([] as FarmerSearchResult[]))
        );
      })
    ).subscribe({
      next: (farmers: FarmerSearchResult[]) => {
        this.searchedFarmers = farmers;
        this.showFarmerDropdown = farmers.length > 0;
      },
      error: (error) => {
        console.error('Search error:', error);
        this.searchedFarmers = [];
        this.showFarmerDropdown = false;
      }
    });
  }

  onFarmerSearchInput(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.farmerSearchTerm = term;
    this.farmerSearchSubject.next(term);
  }

  searchFarmers(searchTerm: string): Observable<FarmerSearchResult[]> {
    return this.userService.getUsers(0, 20, {
      type: 'FARMER',
      searchTerm: searchTerm,
      status: 'ACTIVE'
    }).pipe(
      switchMap((response: any) => {
        if (!response || !response.content) {
          return of([] as FarmerSearchResult[]);
        }

        const farmers: FarmerSearchResult[] = response.content.map((user: any) => ({
          userId: user.userId || '',
          name: user.name || 'Unknown Farmer',
          phoneNumber: user.phoneNumber || 'N/A',
          agriculturalType: user.farmerDetails?.agriculturalType || '',
          cropTypes: user.farmerDetails?.cropTypes || [],
          landArea: user.farmerDetails?.totalLandArea || 0
        }));

        return of(farmers);
      }),
      catchError((error) => {
        console.error('Error searching farmers:', error);
        return of([] as FarmerSearchResult[]);
      })
    );
  }

  selectFarmer(farmer: FarmerSearchResult): void {
    this.selectedFarmer = farmer;
    this.farmerSearchTerm = farmer.name;
    this.newProduction.farmerId = farmer.userId;
    this.showFarmerDropdown = false;
  }

  clearFarmerSelection(): void {
    this.selectedFarmer = null;
    this.farmerSearchTerm = '';
    this.newProduction.farmerId = '';
    this.searchedFarmers = [];
    this.showFarmerDropdown = false;
  }

  loadDashboardMetrics(): void {
    this.productionService.getDashboardMetrics().subscribe({
      next: (metrics: DashboardMetrics) => {
        this.totalProduction = metrics.totalProduction;
        this.totalProductionPercent = metrics.totalProductionPercent;
        this.activeFarmers = metrics.activeFarmers;
        this.activeFarmersParticipation = metrics.activeFarmersParticipation;
        this.gradeAProduction = metrics.gradeAProduction;
        this.gradeAPercent = metrics.gradeAPercent;
        this.thisMonthDeliveries = metrics.thisMonthDeliveries;
        this.thisMonthChange = metrics.thisMonthChange;
      },
      error: (error: any) => {
        console.error('Error loading metrics:', error);
        this.useMockMetrics();
      }
    });
  }

  useMockMetrics(): void {
    this.totalProduction = '287.5 MT';
    this.totalProductionPercent = '+15.2% vs last cycle';
    this.activeFarmers = 245;
    this.activeFarmersParticipation = '81.7% participation';
    this.gradeAProduction = '168.3 MT';
    this.gradeAPercent = '58.5% premium';
    this.thisMonthDeliveries = '42.8 MT';
    this.thisMonthChange = '+8.3% vs last month';
  }

  loadProductions(): void {
    const params: any = {
      page: this.currentPage - 1,
      size: this.pageSize
    };

    if (this.selectedCrop !== 'All Crops') {
      params.cropType = this.selectedCrop;
    }
    if (this.selectedGrade !== 'All Grades') {
      params.qualityGrade = this.selectedGrade;
    }
    if (this.searchQuery) {
      params.searchTerm = this.searchQuery;
    }

    this.productionService.getProductions(params).subscribe({
      next: (response: any) => {
        this.productionRecords = response.content || [];
        this.filteredProduction = this.productionRecords.map((record: ProductionRecord) =>
          this.transformRecord(record)
        );
        this.totalPages = response.totalPages || 1;
        this.updatePaginatedData();
      },
      error: (error: any) => {
        console.error('Error loading productions:', error);
        this.filteredProduction = [];
        this.paginatedProduction = [];
      }
    });
  }

  transformRecord(record: ProductionRecord): any {
    return {
      id: record.productionId,
      date: new Date(record.deliveryDate).toLocaleDateString('en-GB'),
      farmer: record.farmerName,
      crop: this.getCropDisplayName(record.cropType),
      quantity: record.quantity + ' MT',
      grade: this.getGradeDisplayName(record.qualityGrade),
      gradeClass: this.getGradeClass(record.qualityGrade),
      warehouse: record.warehouse,
      value: this.formatValue(record.valueXaf),
      status: this.getStatusDisplayName(record.status),
      statusClass: this.getStatusClass(record.status)
    };
  }

  getCropDisplayName(crop: string): string {
    const cropMap: Record<string, string> = {
      'COCOA': 'Cocoa',
      'COFFEE': 'Coffee',
      'MAIZE': 'Maize',
      'CASSAVA': 'Cassava',
      'RICE': 'Rice',
      'COTTON': 'Cotton'
    };
    return cropMap[crop] || crop;
  }

  getGradeDisplayName(grade: string): string {
    const gradeMap: Record<string, string> = {
      'GRADE_A': 'Grade A',
      'GRADE_B': 'Grade B',
      'GRADE_C': 'Grade C'
    };
    return gradeMap[grade] || grade;
  }

  getGradeClass(grade: string): string {
    const classMap: Record<string, string> = {
      'GRADE_A': 'grade-a',
      'GRADE_B': 'grade-b',
      'GRADE_C': 'grade-c'
    };
    return classMap[grade] || '';
  }

  getStatusDisplayName(status: string): string {
    const statusMap: Record<string, string> = {
      'PENDING': 'Pending',
      'VERIFIED': 'Verified',
      'REJECTED': 'Rejected',
      'PROCESSED': 'Processed',
      'SOLD': 'Sold'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    const classMap: Record<string, string> = {
      'PENDING': 'pending',
      'VERIFIED': 'verified',
      'REJECTED': 'rejected',
      'PROCESSED': 'processed',
      'SOLD': 'sold'
    };
    return classMap[status] || '';
  }

  formatValue(value: number): string {
    return `${value.toLocaleString()} XAF`;
  }

  loadWarehouses(): void {
    this.productionService.getWarehouses().subscribe({
      next: (warehouses: string[]) => {
        this.warehouses = warehouses;
        if (warehouses.length > 0) {
          this.newProduction.warehouse = warehouses[0];
        }
      },
      error: (error: any) => {
        console.error('Error loading warehouses:', error);
        this.warehouses = ['Douala Warehouse', 'Yaoundé Warehouse', 'Garoua Warehouse'];
        this.newProduction.warehouse = this.warehouses[0];
      }
    });
  }

  updatePaginatedData(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedProduction = this.filteredProduction.slice(start, end);
  }

  onRecordProduction(): void {
    this.showRecordModal = true;
    this.resetNewProduction();
  }

  closeRecordModal(): void {
    this.showRecordModal = false;
    this.clearFarmerSelection();
  }

  submitProduction(): void {
    if (!this.isFormValid()) {
      alert('Please fill in all required fields and select a farmer');
      return;
    }

    const request: CreateProductionRequest = {
      farmerId: this.newProduction.farmerId,
      cropType: this.newProduction.cropType,
      quantity: this.newProduction.quantity,
      qualityGrade: this.newProduction.qualityGrade,
      warehouse: this.newProduction.warehouse,
      deliveryDate: this.newProduction.deliveryDate
    };

    this.productionService.createProduction(request).subscribe({
      next: (response: ProductionRecord) => {
        console.log('Production recorded:', response);
        this.closeRecordModal();
        this.loadProductions();
        this.loadDashboardMetrics();
        alert('Production recorded successfully!');
      },
      error: (error: any) => {
        console.error('Error recording production:', error);
        alert('Failed to record production: ' + (error.message || 'Please try again.'));
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.newProduction.farmerId &&
      this.newProduction.cropType &&
      this.newProduction.quantity > 0 &&
      this.newProduction.qualityGrade &&
      this.newProduction.warehouse &&
      this.newProduction.deliveryDate
    );
  }

  resetNewProduction(): void {
    this.newProduction = {
      farmerId: '',
      cropType: 'COCOA',
      quantity: 0,
      qualityGrade: 'GRADE_A',
      warehouse: this.warehouses[0] || '',
      deliveryDate: new Date().toISOString().split('T')[0]
    };
    this.clearFarmerSelection();
  }

  onExport(): void {
    this.showExportModal = true;
  }

  closeExportModal(): void {
    this.showExportModal = false;
  }

  performExport(): void {
    console.log('Exporting data in format:', this.exportFormat);
    alert('Export functionality coming soon!');
    this.closeExportModal();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadProductions();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadProductions();
    }
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadProductions();
  }

  onSearchChange(): void {
    this.currentPage = 1;
    this.loadProductions();
  }
}