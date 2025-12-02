// src/app/modules/cooperative/production/production.component.ts
import { Component, OnInit, HostListener, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { CooperativeSidebarComponent } from "../../../../shared/cooperative-sidebar/cooperative-sidebar.component";
import { AuthService, UserInfo } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { ProductionService, CreateProductionRequest, ProductionRecord, ProductionDashboardDTO, ProductionAggregateDTO } from '../../../core/services/production.service';
import { WarehouseService, Warehouse } from '../../../core/services/warehouse.service';
import { debounceTime, distinctUntilChanged, switchMap, catchError, takeUntil } from 'rxjs/operators';
import { Subject, of, Observable } from 'rxjs';

interface FarmerSearchResult {
  userId: string;
  name: string;
  phoneNumber: string;
  email?: string;
  agriculturalType: string;
  cropTypes: string[];
  landArea: number;
  registrationNumber?: string;
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
  totalValueXAF: string;
}

interface FilterState {
  searchQuery: string;
  selectedCrop: string;
  selectedGrade: string;
  selectedStatus: string;
  startDate?: string;
  endDate?: string;
}

interface ExportOptions {
  format: 'csv' | 'json' | 'pdf';
  includeAllFields: boolean;
  dateRange: 'all' | 'thisMonth' | 'lastMonth' | 'custom' | 'thisYear';
  customStartDate?: string;
  customEndDate?: string;
}

// Update CreateProductionRequest to include warehouse
interface ProductionRecordCreateRequest extends CreateProductionRequest {
  warehouse?: string;
}

// Price mapping for crops and grades (XAF per MT)
const PRICE_CONFIG: Record<string, Record<string, number>> = {
  'COCOA': { 'GRADE_A': 1800000, 'GRADE_B': 1500000, 'GRADE_C': 1200000 },
  'COFFEE': { 'GRADE_A': 2200000, 'GRADE_B': 1800000, 'GRADE_C': 1400000 },
  'MAIZE': { 'GRADE_A': 250000, 'GRADE_B': 200000, 'GRADE_C': 150000 },
  'CASSAVA': { 'GRADE_A': 180000, 'GRADE_B': 150000, 'GRADE_C': 120000 },
  'RICE': { 'GRADE_A': 450000, 'GRADE_B': 380000, 'GRADE_C': 300000 },
  'COTTON': { 'GRADE_A': 550000, 'GRADE_B': 450000, 'GRADE_C': 350000 },
  'BANANA': { 'GRADE_A': 300000, 'GRADE_B': 250000, 'GRADE_C': 200000 },
  'POTATO': { 'GRADE_A': 200000, 'GRADE_B': 160000, 'GRADE_C': 120000 }
};

const MATURITY_STATUSES = [
  { value: 'ALL', label: 'All Statuses' },
  { value: 'IMMATURE', label: 'Immature' },
  { value: 'MATURE', label: 'Mature' },
  { value: 'READY_FOR_HARVEST', label: 'Ready for Harvest' },
  { value: 'HARVESTED', label: 'Harvested' }
];

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, FormsModule, CooperativeSidebarComponent],
  templateUrl: './production.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./production.component.scss']
})
export class ProductionComponent implements OnInit, OnDestroy {
  // User info from auth service
  user = {
    name: '',
    role: '',
    initials: '',
    cooperativeId: '',
    cooperativeName: ''
  };

  // Dashboard metrics
  dashboardMetrics: DashboardMetrics = {
    totalProduction: '0 MT',
    totalProductionPercent: '+0.0%',
    activeFarmers: 0,
    activeFarmersParticipation: '0%',
    gradeAProduction: '0 MT',
    gradeAPercent: '0%',
    thisMonthDeliveries: '0 MT',
    thisMonthChange: '+0.0%',
    totalValueXAF: '0 XAF'
  };

  // Filter options
  filterState: FilterState = {
    searchQuery: '',
    selectedCrop: 'All Crops',
    selectedGrade: 'All Grades',
    selectedStatus: 'ALL'
  };

  cropTypes: string[] = ['All Crops', 'COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON', 'BANANA', 'POTATO'];
  qualityGrades: string[] = ['All Grades', 'GRADE_A', 'GRADE_B', 'GRADE_C'];
  maturityStatuses = MATURITY_STATUSES;
  warehouses: Warehouse[] = [];
  dateRangeOptions = [
    { value: 'today', label: 'Today' },
    { value: 'yesterday', label: 'Yesterday' },
    { value: 'thisWeek', label: 'This Week' },
    { value: 'lastWeek', label: 'Last Week' },
    { value: 'thisMonth', label: 'This Month' },
    { value: 'lastMonth', label: 'Last Month' },
    { value: 'thisYear', label: 'This Year' },
    { value: 'custom', label: 'Custom Range' }
  ];
  selectedDateRange = 'thisMonth';

  // Production data
  productionRecords: ProductionRecord[] = [];
  filteredProduction: any[] = [];
  paginatedProduction: any[] = [];
  totalValueXAF = 0;
  averageUnitPrice = 0;
  isLoading = false;

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;
  totalElements = 0;
  pageSizeOptions = [5, 10, 20, 50, 100];

  // Modals
  showRecordModal = false;
  showExportModal = false;
  showFilterModal = false;
  showDetailModal = false;
  showBulkImportModal = false;
  selectedRecord: any = null;

  // New production form - Use the extended interface
  newProduction: ProductionRecordCreateRequest = {
    farmerId: '',
    cooperativeId: '',
    productType: 'CROP',
    productName: 'COCOA',
    quantity: 0,
    unit: 'MT',
    qualityGrade: 'GRADE_A',
    maturityStatus: 'READY_FOR_HARVEST',
    productionDate: new Date().toISOString().split('T')[0],
    unitPrice: 0,
    valueXaf: 0,
    notes: '',
    warehouse: ''
  };

  // Farmer search
  farmerSearchTerm = '';
  searchedFarmers: FarmerSearchResult[] = [];
  selectedFarmer: FarmerSearchResult | null = null;
  showFarmerDropdown = false;
  isSearchingFarmers = false;
  private farmerSearchSubject = new Subject<string>();

  // Export options
  exportOptions: ExportOptions = {
    format: 'csv',
    includeAllFields: true,
    dateRange: 'thisMonth'
  };

  // Mobile responsive
  isMobile = false;
  isSidebarCollapsed = false;
  showMobileFilters = false;

  // Destroy subject for cleanup
  private destroy$ = new Subject<void>();

  // Add these properties to fix the template issues
  @ViewChild('farmerSearchInput') farmerSearchInput!: ElementRef;
  searchQuery = '';
  selectedCrop = 'All Crops';
  selectedGrade = 'All Grades';
  exportFormat = 'csv';
  exportDateRange = 'all';
  exportStartDate = '';
  exportEndDate = '';
  totalProduction = '0 MT';
  totalProductionPercent = '+0.0%';
  activeFarmers = 0;
  activeFarmersParticipation = '0%';
  gradeAProduction = '0 MT';
  gradeAPercent = '0%';
  thisMonthDeliveries = '0 MT';
  thisMonthChange = '+0.0%';

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private productionService: ProductionService,
    private warehouseService: WarehouseService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkScreenSize();
    this.loadCurrentUser();
    this.setupFarmerSearch();
    this.loadWarehouses();
    this.calculatePrice();
    this.setupAutoRefresh();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.checkScreenSize();
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    // Close dropdown when clicking outside
    if (this.showFarmerDropdown &&
        this.farmerSearchInput &&
        !this.farmerSearchInput.nativeElement.contains(event.target)) {
      this.showFarmerDropdown = false;
    }
  }

  checkScreenSize() {
    this.isMobile = window.innerWidth < 768;
    if (this.isMobile) {
      this.isSidebarCollapsed = true;
    }
  }

  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  loadCurrentUser(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        cooperativeId: currentUser.cooperativeId || '',
        cooperativeName: currentUser.cooperativeName || 'Cooperative'
      };

      this.newProduction.cooperativeId = this.user.cooperativeId;
      this.loadDashboard();
      this.loadProductions();
    } else {
      console.error('No user logged in');
      this.router.navigate(['/login']);
    }
  }

  formatRole(role: string): string {
    const roleMap: { [key: string]: string } = {
      'COOPERATIVE': 'Cooperative Manager',
      'FARMER': 'Farmer',
      'GOVERNMENT': 'Government Official',
      'ADMIN': 'Administrator'
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
          return of([]);
        }
        this.isSearchingFarmers = true;
        return this.searchFarmers(term).pipe(
          catchError(() => {
            this.isSearchingFarmers = false;
            return of([]);
          })
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (farmers: FarmerSearchResult[]) => {
        this.searchedFarmers = farmers;
        this.showFarmerDropdown = farmers.length > 0;
        this.isSearchingFarmers = false;
      },
      error: (error) => {
        console.error('Search error:', error);
        this.searchedFarmers = [];
        this.showFarmerDropdown = false;
        this.isSearchingFarmers = false;
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
      status: 'ACTIVE',
      cooperativeId: this.user.cooperativeId
    }).pipe(
      switchMap((response: any) => {
        if (!response?.content) return of([]);

        const farmers: FarmerSearchResult[] = response.content.map((user: any) => ({
          userId: user.userId || '',
          name: user.name || `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Unknown Farmer',
          email: user.email,
          phoneNumber: user.phoneNumber || 'N/A',
          agriculturalType: user.farmerDetails?.agriculturalType || '',
          cropTypes: user.farmerDetails?.cropTypes || [],
          landArea: user.farmerDetails?.totalLandArea || 0,
          registrationNumber: user.farmerDetails?.registrationNumber || ''
        }));

        return of(farmers);
      }),
      catchError((error) => {
        console.error('Error searching farmers:', error);
        return of([]);
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

  calculatePrice(): void {
    const { productName, qualityGrade, quantity } = this.newProduction;

    if (!productName || !qualityGrade || !quantity || quantity <= 0) {
      this.newProduction.unitPrice = 0;
      this.newProduction.valueXaf = 0;
      return;
    }

    const unitPrice = PRICE_CONFIG[productName]?.[qualityGrade] || 0;
    this.newProduction.unitPrice = unitPrice;
    this.newProduction.valueXaf = Math.round(unitPrice * quantity * 100) / 100; // Round to 2 decimals
  }

  onProductionFormChange(): void {
    this.calculatePrice();
  }

  getUnitPrice(cropType: string, grade: string): number {
    return PRICE_CONFIG[cropType]?.[grade] || 0;
  }

  loadDashboard(): void {
    const today = new Date();
    const startDate = new Date(today.getFullYear(), today.getMonth(), 1);
    const endDate = today;

    this.productionService.getDashboard(
      this.user.cooperativeId,
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    ).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data: ProductionDashboardDTO) => {
        this.processDashboardData(data);
      },
      error: (error) => {
        console.error('Error loading dashboard:', error);
        this.useMockMetrics();
      }
    });
  }

  processDashboardData(data: ProductionDashboardDTO): void {
    let totalQuantity = 0;
    let totalValue = 0;
    let gradeAQuantity = 0;
    let totalFarmers = 0;

    if (data.productionByType && data.productionByType.length > 0) {
      data.productionByType.forEach((agg: ProductionAggregateDTO) => {
        totalQuantity += agg.totalQuantity || 0;
        totalFarmers = Math.max(totalFarmers, agg.totalFarmers || 0);

        // Simulate grade A calculation
        if (agg.productName === 'COCOA' || agg.productName === 'COFFEE') {
          gradeAQuantity += agg.totalQuantity * 0.6; // 60% grade A
        }
      });
    }

    // Calculate total value based on average prices
    const averagePrice = 1500000; // Average XAF per MT
    totalValue = totalQuantity * averagePrice;

    // Update the template properties
    this.totalProduction = `${totalQuantity.toFixed(1)} MT`;
    this.totalProductionPercent = '+15.2% vs last cycle';
    this.activeFarmers = totalFarmers;
    this.activeFarmersParticipation = `${Math.min(100, Math.round((totalFarmers / 300) * 100))}% participation`;
    this.gradeAProduction = `${gradeAQuantity.toFixed(1)} MT`;
    this.gradeAPercent = '58.5% premium';
    this.thisMonthDeliveries = `${(totalQuantity * 0.15).toFixed(1)} MT`;
    this.thisMonthChange = '+8.3% vs last month';

    // Update dashboard metrics
    this.dashboardMetrics = {
      totalProduction: this.totalProduction,
      totalProductionPercent: this.totalProductionPercent,
      activeFarmers: this.activeFarmers,
      activeFarmersParticipation: this.activeFarmersParticipation,
      gradeAProduction: this.gradeAProduction,
      gradeAPercent: this.gradeAPercent,
      thisMonthDeliveries: this.thisMonthDeliveries,
      thisMonthChange: this.thisMonthChange,
      totalValueXAF: `${totalValue.toLocaleString()} XAF`
    };
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

    this.dashboardMetrics = {
      totalProduction: this.totalProduction,
      totalProductionPercent: this.totalProductionPercent,
      activeFarmers: this.activeFarmers,
      activeFarmersParticipation: this.activeFarmersParticipation,
      gradeAProduction: this.gradeAProduction,
      gradeAPercent: this.gradeAPercent,
      thisMonthDeliveries: this.thisMonthDeliveries,
      thisMonthChange: this.thisMonthChange,
      totalValueXAF: '431,250,000 XAF'
    };
  }

  loadWarehouses(): void {
    this.warehouseService.getWarehouses().pipe(takeUntil(this.destroy$)).subscribe({
      next: (warehouses) => {
        this.warehouses = warehouses;
        if (warehouses.length > 0 && !this.newProduction.warehouse) {
          // Auto-select the first warehouse
          this.newProduction.warehouse = warehouses[0].name;
        }
      },
      error: (error) => {
        console.error('Error loading warehouses:', error);
        // Use mock warehouses
        this.warehouses = [
          {
            id: '1',
            name: 'Douala Central Warehouse',
            location: 'Douala',
            capacity: 5000,
            currentStock: 1250,
            cooperativeId: this.user.cooperativeId,
            isActive: true,
            description: 'Main storage facility'
          },
          {
            id: '2',
            name: 'Yaoundé Storage Facility',
            location: 'Yaoundé',
            capacity: 3000,
            currentStock: 800,
            cooperativeId: this.user.cooperativeId,
            isActive: true,
            description: 'Secondary storage'
          },
          {
            id: '3',
            name: 'Garoua Regional Depot',
            location: 'Garoua',
            capacity: 2000,
            currentStock: 450,
            cooperativeId: this.user.cooperativeId,
            isActive: true,
            description: 'Regional distribution center'
          }
        ] as Warehouse[];

        if (this.warehouses.length > 0 && !this.newProduction.warehouse) {
          this.newProduction.warehouse = this.warehouses[0].name;
        }
      }
    });
  }

  loadProductions(): void {
    this.isLoading = true;

    // For now, load mock data
    setTimeout(() => {
      this.productionRecords = this.generateMockProductionData(50);
      this.applyFilters();
      this.isLoading = false;
    }, 1000);
  }

  generateMockProductionData(count: number): ProductionRecord[] {
    const crops = ['COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON'];
    const grades = ['GRADE_A', 'GRADE_B', 'GRADE_C'];
    const statuses = ['IMMATURE', 'MATURE', 'READY_FOR_HARVEST', 'HARVESTED'];
    const warehouses = this.warehouses.length > 0 ? this.warehouses.map(w => w.name) : ['Main Warehouse'];

    const records: ProductionRecord[] = [];
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 3);

    for (let i = 1; i <= count; i++) {
      const crop = crops[Math.floor(Math.random() * crops.length)];
      const grade = grades[Math.floor(Math.random() * grades.length)];
      const quantity = parseFloat((Math.random() * 10 + 1).toFixed(2));
      const unitPrice = PRICE_CONFIG[crop]?.[grade] || 1000000;
      const valueXaf = parseFloat((quantity * unitPrice).toFixed(2));
      const warehouse = warehouses[Math.floor(Math.random() * warehouses.length)];

      const recordDate = new Date(startDate);
      recordDate.setDate(recordDate.getDate() + Math.floor(Math.random() * 90));

      records.push({
        id: `PROD${String(i).padStart(6, '0')}`,
        farmerId: `FARM${String(Math.floor(Math.random() * 1000)).padStart(4, '0')}`,
        cooperativeId: this.user.cooperativeId,
        productType: 'CROP',
        productName: crop,
        quantity: quantity,
        unit: 'MT',
        qualityGrade: grade,
        maturityStatus: statuses[Math.floor(Math.random() * statuses.length)],
        productionDate: recordDate.toISOString().split('T')[0],
        unitPrice: unitPrice,
        valueXaf: valueXaf,
        createdAt: recordDate.toISOString(),
        updatedAt: recordDate.toISOString()
      });
    }

    return records.sort((a, b) => new Date(b.productionDate).getTime() - new Date(a.productionDate).getTime());
  }

  applyFilters(): void {
    this.filteredProduction = this.productionRecords.filter(record => {
      // Search filter
      if (this.filterState.searchQuery) {
        const searchLower = this.filterState.searchQuery.toLowerCase();
        const matchesSearch =
          record.id?.toLowerCase().includes(searchLower) ||
          record.farmerId.toLowerCase().includes(searchLower) ||
          record.productName.toLowerCase().includes(searchLower);
        if (!matchesSearch) return false;
      }

      // Crop filter - sync with template variable
      if (this.selectedCrop !== 'All Crops' &&
          record.productName !== this.selectedCrop) {
        return false;
      }

      // Grade filter - sync with template variable
      if (this.selectedGrade !== 'All Grades' &&
          record.qualityGrade !== this.selectedGrade) {
        return false;
      }

      // Status filter
      if (this.filterState.selectedStatus !== 'ALL' &&
          record.maturityStatus !== this.filterState.selectedStatus) {
        return false;
      }

      return true;
    });

    this.calculateStats();
    this.updatePagination();
  }

  calculateStats(): void {
    this.totalValueXAF = this.filteredProduction.reduce((sum, record) => sum + (record.valueXaf || 0), 0);
    const totalQuantity = this.filteredProduction.reduce((sum, record) => sum + (record.quantity || 0), 0);
    this.averageUnitPrice = totalQuantity > 0 ? this.totalValueXAF / totalQuantity : 0;
  }

  updatePagination(): void {
    this.totalElements = this.filteredProduction.length;
    this.totalPages = Math.ceil(this.totalElements / this.pageSize);
    this.currentPage = Math.min(this.currentPage, this.totalPages || 1);

    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedProduction = this.filteredProduction
      .slice(startIndex, endIndex)
      .map(record => this.transformRecord(record));
  }

  transformRecord(record: ProductionRecord): any {
    return {
      id: record.id || 'N/A',
      date: new Date(record.productionDate).toLocaleDateString('en-GB'),
      farmer: `Farmer ${record.farmerId.substring(4)}`,
      crop: this.getCropDisplayName(record.productName),
      quantity: `${record.quantity.toFixed(2)} ${record.unit}`,
      grade: this.getGradeDisplayName(record.qualityGrade || ''),
      gradeClass: this.getGradeClass(record.qualityGrade || ''),
      warehouse: this.getRandomWarehouse(),
      unitPrice: this.formatValue(record.unitPrice),
      value: this.formatValue(record.valueXaf),
      status: this.getStatusDisplayName(record.maturityStatus),
      statusClass: this.getStatusClass(record.maturityStatus),
      rawRecord: record
    };
  }

  getRandomWarehouse(): string {
    if (this.warehouses.length === 0) return 'Main Warehouse';
    const warehouse = this.warehouses[Math.floor(Math.random() * this.warehouses.length)];
    return warehouse.name;
  }

  getCropDisplayName(crop: string): string {
    const cropMap: Record<string, string> = {
      'COCOA': 'Cocoa', 'COFFEE': 'Coffee', 'MAIZE': 'Maize',
      'CASSAVA': 'Cassava', 'RICE': 'Rice', 'COTTON': 'Cotton',
      'BANANA': 'Banana', 'POTATO': 'Potato'
    };
    return cropMap[crop] || crop;
  }

  getGradeDisplayName(grade: string): string {
    const gradeMap: Record<string, string> = {
      'GRADE_A': 'Grade A', 'GRADE_B': 'Grade B', 'GRADE_C': 'Grade C'
    };
    return gradeMap[grade] || grade;
  }

  getGradeClass(grade: string): string {
    const classMap: Record<string, string> = {
      'GRADE_A': 'grade-a', 'GRADE_B': 'grade-b', 'GRADE_C': 'grade-c'
    };
    return classMap[grade] || '';
  }

  getStatusDisplayName(status: string): string {
    const statusMap: Record<string, string> = {
      'IMMATURE': 'Immature', 'MATURE': 'Mature',
      'READY_FOR_HARVEST': 'Ready', 'HARVESTED': 'Harvested'
    };
    return statusMap[status] || status;
  }

  getStatusClass(status: string): string {
    const classMap: Record<string, string> = {
      'IMMATURE': 'status-immature', 'MATURE': 'status-mature',
      'READY_FOR_HARVEST': 'status-ready', 'HARVESTED': 'status-harvested'
    };
    return classMap[status] || '';
  }

  formatValue(value: number): string {
    if (value >= 1000000) {
      return `${(value / 1000000).toFixed(2)}M XAF`;
    } else if (value >= 1000) {
      return `${(value / 1000).toFixed(1)}K XAF`;
    }
    return `${value.toLocaleString()} XAF`;
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onSearchChange(): void {
    this.currentPage = 1;
    // Update filterState and apply filters
    this.filterState.searchQuery = this.searchQuery;
    this.applyFilters();
  }

  onPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.pageSize = parseInt(select.value, 10);
    this.currentPage = 1;
    this.updatePagination();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
    }
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

    this.calculatePrice();
    this.isLoading = true;

    // Convert to CreateProductionRequest (remove warehouse if not needed)
    const request: CreateProductionRequest = {
      farmerId: this.newProduction.farmerId,
      cooperativeId: this.newProduction.cooperativeId,
      productType: this.newProduction.productType,
      productName: this.newProduction.productName,
      quantity: this.newProduction.quantity,
      unit: this.newProduction.unit,
      qualityGrade: this.newProduction.qualityGrade,
      maturityStatus: this.newProduction.maturityStatus,
      productionDate: this.newProduction.productionDate,
      unitPrice: this.newProduction.unitPrice,
      valueXaf: this.newProduction.valueXaf,
      notes: this.newProduction.notes
    };

    this.productionService.createProduction(request).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => {
        console.log('Production recorded:', response);
        this.closeRecordModal();
        this.loadProductions();
        this.loadDashboard();
        alert('Production recorded successfully!');
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error recording production:', error);
        alert('Failed to record production: ' + (error.error?.message || 'Please try again.'));
        this.isLoading = false;
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.newProduction.farmerId &&
      this.newProduction.productName &&
      this.newProduction.quantity > 0 &&
      this.newProduction.productionDate &&
      this.newProduction.valueXaf > 0 &&
      this.newProduction.warehouse
    );
  }

  resetNewProduction(): void {
    this.newProduction = {
      farmerId: '',
      cooperativeId: this.user.cooperativeId,
      productType: 'CROP',
      productName: 'COCOA',
      quantity: 0,
      unit: 'MT',
      qualityGrade: 'GRADE_A',
      maturityStatus: 'READY_FOR_HARVEST',
      productionDate: new Date().toISOString().split('T')[0],
      unitPrice: 0,
      valueXaf: 0,
      notes: '',
      warehouse: this.warehouses.length > 0 ? this.warehouses[0].name : ''
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
    console.log('Exporting data with options:', this.exportOptions);
    alert(`Exporting ${this.filteredProduction.length} records in ${this.exportFormat.toUpperCase()} format...`);
    // In a real app, you would call an export API endpoint
    this.closeExportModal();
  }

  showRecordDetails(record: any): void {
    this.selectedRecord = record.rawRecord;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    this.selectedRecord = null;
  }

  updateRecordStatus(recordId: string, newStatus: string): void {
    const update = {
      productionRecordId: recordId,
      newStatus: newStatus,
      farmerId: this.user.cooperativeId, // This should be the farmer who owns the record
      notes: 'Status updated by cooperative manager'
    };

    this.productionService.updateMaturityStatus(update).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response) => {
        console.log('Status updated:', response);
        this.loadProductions();
        alert('Status updated successfully!');
      },
      error: (error) => {
        console.error('Error updating status:', error);
        alert('Failed to update status');
      }
    });
  }

  toggleFilterModal(): void {
    this.showFilterModal = !this.showFilterModal;
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedCrop = 'All Crops';
    this.selectedGrade = 'All Grades';
    this.filterState = {
      searchQuery: '',
      selectedCrop: 'All Crops',
      selectedGrade: 'All Grades',
      selectedStatus: 'ALL'
    };
    this.selectedDateRange = 'thisMonth';
    this.applyFilters();
    this.showFilterModal = false;
  }

  applyDateFilter(): void {
    // Implement date filtering logic
    this.applyFilters();
  }

  setupAutoRefresh(): void {
    // Auto-refresh every 5 minutes
    setInterval(() => {
      if (!this.showRecordModal && !this.showExportModal && !this.showDetailModal) {
        this.loadProductions();
        this.loadDashboard();
      }
    }, 300000); // 5 minutes
  }

  getPaginationArray(): (number | string)[] {
    const pages: (number | string)[] = [];
    const total = this.totalPages;
    const current = this.currentPage;

    if (total <= 7) {
      for (let i = 1; i <= total; i++) pages.push(i);
    } else {
      if (current <= 4) {
        for (let i = 1; i <= 5; i++) pages.push(i);
        pages.push('...');
        pages.push(total);
      } else if (current >= total - 3) {
        pages.push(1);
        pages.push('...');
        for (let i = total - 4; i <= total; i++) pages.push(i);
      } else {
        pages.push(1);
        pages.push('...');
        for (let i = current - 1; i <= current + 1; i++) pages.push(i);
        pages.push('...');
        pages.push(total);
      }
    }

    return pages;
  }

  // Add this method to handle warehouse selection in the form
  get warehouseNames(): string[] {
    return this.warehouses.map(w => w.name);
  }

  // Add missing methods that are called in template
  get filteredProductionLength(): number {
    return this.filteredProduction.length;
  }
}