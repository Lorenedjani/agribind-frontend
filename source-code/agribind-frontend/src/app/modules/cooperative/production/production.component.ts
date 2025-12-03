import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViewEncapsulation } from '@angular/core';
import { CooperativeSidebarComponent } from "../../../../shared/cooperative-sidebar/cooperative-sidebar.component";
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { MockProductionService, ExtendedProductionRecord } from '../../../core/services/production.service.mock';
import { ProductionRecord, CreateProductionRequest } from '../../../core/services/production.service';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { Subject, of, Observable } from 'rxjs';

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

interface ApiUserResponse {
  content: any[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

// Price mapping for crops and grades
interface PriceConfig {
  [cropType: string]: {
    [grade: string]: number;
  };
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
  productionRecords: ExtendedProductionRecord[] = [];
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
  newProduction = {
    farmerId: '',
    productName: 'COCOA',
    quantity: 0,
    qualityGrade: 'GRADE_A',
    warehouse: '',
    productionDate: new Date().toISOString().split('T')[0],
    unitPrice: 0,
    valueXaf: 0
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

  // Price configuration (XAF per MT)
  private priceConfig: PriceConfig = {
    'COCOA': {
      'GRADE_A': 1800000,
      'GRADE_B': 1500000,
      'GRADE_C': 1200000
    },
    'COFFEE': {
      'GRADE_A': 2200000,
      'GRADE_B': 1800000,
      'GRADE_C': 1400000
    },
    'MAIZE': {
      'GRADE_A': 250000,
      'GRADE_B': 200000,
      'GRADE_C': 150000
    },
    'CASSAVA': {
      'GRADE_A': 180000,
      'GRADE_B': 150000,
      'GRADE_C': 120000
    },
    'RICE': {
      'GRADE_A': 450000,
      'GRADE_B': 380000,
      'GRADE_C': 300000
    },
    'COTTON': {
      'GRADE_A': 550000,
      'GRADE_B': 450000,
      'GRADE_C': 350000
    }
  };

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private productionService: MockProductionService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.setupFarmerSearch();
    this.loadWarehouses();
    this.calculatePrice(); // Initialize price
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

  // Calculate price based on crop type, grade, and quantity
  calculatePrice(): void {
    const { productName, qualityGrade, quantity } = this.newProduction;

    if (!productName || !qualityGrade || !quantity || quantity <= 0) {
      this.newProduction.unitPrice = 0;
      this.newProduction.valueXaf = 0;
      return;
    }

    // Get unit price from config
    const unitPrice = this.priceConfig[productName]?.[qualityGrade] || 0;
    this.newProduction.unitPrice = unitPrice;

    // Calculate total value
    this.newProduction.valueXaf = Math.round(unitPrice * quantity);
  }

  // Event handler for form changes
  onProductionFormChange(): void {
    this.calculatePrice();
  }

  // Get unit price for display
  getUnitPrice(cropType: string, grade: string): number {
    return this.priceConfig[cropType]?.[grade] || 0;
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
        this.filteredProduction = this.productionRecords.map((record: ExtendedProductionRecord) =>
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

  transformRecord(record: ExtendedProductionRecord): any {
    return {
      id: record.productionId,
      date: new Date(record.deliveryDate).toLocaleDateString('en-GB'),
      farmer: record.farmerName,
      crop: this.getCropDisplayName(record.cropType),
      quantity: record.quantity + ' MT',
      grade: this.getGradeDisplayName(record.qualityGrade || 'GRADE_C'),
      gradeClass: this.getGradeClass(record.qualityGrade || 'GRADE_C'),
      warehouse: record.warehouse,
      unitPrice: this.formatValue(record.unitPrice),
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

    // Ensure price is calculated
    this.calculatePrice();

    const request: CreateProductionRequest = {
      farmerId: this.newProduction.farmerId,
      cooperativeId: this.user.cooperativeId,
      productType: 'CROP',
      productName: this.newProduction.productName,
      quantity: this.newProduction.quantity,
      unit: 'MT',
      qualityGrade: this.newProduction.qualityGrade,
      maturityStatus: 'PENDING',
      productionDate: this.newProduction.productionDate,
      unitPrice: this.newProduction.unitPrice,
      valueXaf: this.newProduction.valueXaf,
      notes: this.newProduction.warehouse
    };

    this.productionService.createProduction(request).subscribe({
      next: (response: ExtendedProductionRecord) => {
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
      this.newProduction.productName &&
      this.newProduction.quantity > 0 &&
      this.newProduction.qualityGrade &&
      this.newProduction.warehouse &&
      this.newProduction.productionDate &&
      this.newProduction.valueXaf > 0
    );
  }

  resetNewProduction(): void {
    this.newProduction = {
      farmerId: '',
      productName: 'COCOA',
      quantity: 0,
      qualityGrade: 'GRADE_A',
      warehouse: this.warehouses[0] || '',
      productionDate: new Date().toISOString().split('T')[0],
      unitPrice: 0,
      valueXaf: 0
    };
    this.clearFarmerSelection();
    this.calculatePrice();
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