import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ViewEncapsulation } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { MockProductionService, ExtendedProductionRecord } from '../../../core/services/production.service.mock';
import { ProductionService, ProductionRecord, CreateProductionRequest } from '../../../core/services/production.service';
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

interface ProductListing {
  id: string;
  farmerName: string;
  productName: string;
  cropType: string;
  quantity: number;
  unit: string;
  pricePerUnit: number;
  status: 'listed' | 'sold' | 'pending' | 'cancelled';
  listingDate: string;
  buyerName?: string;
  saleDate?: string;
  commission: number;
}

interface MarketPrice {
  crop: string;
  localPrice: number;
  regionalPrice: number;
  trend: 'up' | 'down' | 'stable';
  lastUpdated: string;
}

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  // Action buttons for different sections
  actionButtons = [
    {
      name: 'Production',
      icon: 'fas fa-tractor',
      active: true
    },
    {
      name: 'Sales & Market',
      icon: 'fas fa-shopping-cart',
      active: false
    }
  ];

  // Sales & Market Data
  marketPrices: MarketPrice[] = [
    {
      crop: 'Cocoa',
      localPrice: 1200000,
      regionalPrice: 1150000,
      trend: 'up',
      lastUpdated: '2025-01-15'
    },
    {
      crop: 'Coffee',
      localPrice: 900000,
      regionalPrice: 950000,
      trend: 'stable',
      lastUpdated: '2025-01-15'
    },
    {
      crop: 'Palm Oil',
      localPrice: 800000,
      regionalPrice: 850000,
      trend: 'down',
      lastUpdated: '2025-01-15'
    },
    {
      crop: 'Cassava',
      localPrice: 150000,
      regionalPrice: 145000,
      trend: 'up',
      lastUpdated: '2025-01-15'
    }
  ];

  productListings: ProductListing[] = [
    {
      id: 'PROD-001',
      farmerName: 'Jean Baptiste',
      productName: 'Cocoa - Grade A',
      cropType: 'Cocoa',
      quantity: 2.5,
      unit: 'MT',
      pricePerUnit: 1200000,
      status: 'listed',
      listingDate: '2025-01-10',
      commission: 150000
    },
    {
      id: 'PROD-002',
      farmerName: 'Marie Kouam',
      productName: 'Coffee - Grade B',
      cropType: 'Coffee',
      quantity: 1.8,
      unit: 'MT',
      pricePerUnit: 900000,
      status: 'sold',
      listingDate: '2025-01-08',
      buyerName: 'ABC Trading Co.',
      saleDate: '2025-01-12',
      commission: 81000
    },
    {
      id: 'PROD-003',
      farmerName: 'Paul Mbarga',
      productName: 'Palm Oil',
      cropType: 'Palm Oil',
      quantity: 3.2,
      unit: 'MT',
      pricePerUnit: 800000,
      status: 'pending',
      listingDate: '2025-01-14',
      commission: 128000
    }
  ];

  // Modal states for sales
  showNewProductModal = false;
  showViewProductModal = false;
  showEditProductModal = false;
  selectedProduct: ProductListing | null = null;

  // Section state getters
  get isProductionSectionActive(): boolean {
    const productionBtn = this.actionButtons.find(btn => btn.name === 'Production');
    return productionBtn ? productionBtn.active : false;
  }

  get isSalesMarketSectionActive(): boolean {
    const salesBtn = this.actionButtons.find(btn => btn.name === 'Sales & Market');
    return salesBtn ? salesBtn.active : false;
  }

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
  maturityStatuses: { value: string; label: string; description: string }[] = [
    { value: 'IMMATURE', label: 'Immature', description: 'Crop is still growing' },
    { value: 'MATURE', label: 'Mature', description: 'Crop has reached maturity' },
    { value: 'READY_FOR_HARVEST', label: 'Ready for Harvest', description: 'Crop is ready to be harvested' },
    { value: 'HARVESTED', label: 'Harvested', description: 'Crop has been harvested' }
  ];
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
  showViewModal = false;
  showEditModal = false;
  selectedRecord: any = null;
  editingRecord: any = null;

  // New production form
  newProduction = {
    farmerId: '',
    productName: 'COCOA',
    quantity: 0,
    unit: 'MT', // MT = Metric Tons, KG = Kilograms, etc.
    qualityGrade: 'GRADE_A',
    maturityStatus: 'HARVESTED',
    warehouse: '',
    productionDate: new Date().toISOString().split('T')[0],
    unitPrice: 0,
    valueXaf: 0
  };

  // Available units for production
  availableUnits: string[] = ['MT', 'KG', 'LITERS', 'BAGS', 'BOXES'];

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
    private productionService: ProductionService
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
      console.log('Current user from auth:', currentUser);

      // For COOPERATIVE users, their cooperativeId is their own userId
      // If cooperativeId is not set, use userId as fallback
      let cooperativeId = currentUser.cooperativeId;

      if (!cooperativeId && currentUser.role === 'COOPERATIVE') {
        cooperativeId = currentUser.userId;
        console.log('Using userId as cooperativeId for COOPERATIVE user:', cooperativeId);
      }

      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        cooperativeId: cooperativeId || ''
      };

      console.log('User object set:', this.user);

      if (!this.user.cooperativeId) {
        console.warn('Cooperative ID not available. User role:', currentUser.role, 'User ID:', currentUser.userId);
        // Try to fetch user details to get cooperativeId
        if (currentUser.userId) {
          this.userService.getUserById(currentUser.userId).subscribe({
            next: (userDetails: any) => {
              console.log('User details fetched:', userDetails);
              // For cooperative users, their ID is their cooperativeId
              if (userDetails.type === 'COOPERATIVE' || userDetails.type === 'COOPERATIVE') {
                this.user.cooperativeId = userDetails.userId || userDetails.id || currentUser.userId;
                console.log('Cooperative ID set from user details:', this.user.cooperativeId);
                this.loadDashboardMetrics();
                this.loadProductions();
              } else {
                console.error('User is not a COOPERATIVE type:', userDetails.type);
              }
            },
            error: (error) => {
              console.error('Error fetching user details:', error);
              // Fallback: use userId if available
              if (currentUser.userId && currentUser.role === 'COOPERATIVE') {
                this.user.cooperativeId = currentUser.userId;
                this.loadDashboardMetrics();
                this.loadProductions();
              }
            }
          });
        }
      } else {
        this.loadDashboardMetrics();
        this.loadProductions();
      }
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

  // Handle action button clicks
  onActionButtonClick(buttonName: string): void {
    this.actionButtons.forEach(btn => btn.active = false);
    const clickedButton = this.actionButtons.find(btn => btn.name === buttonName);
    if (clickedButton) {
      clickedButton.active = true;
    }
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
    console.log('Farmer selected:', farmer, 'Farmer ID:', farmer.userId);
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
    // Use mock data for dashboard metrics for now
    // TODO: Implement real dashboard API call
    this.totalProduction = '125.5 MT';
    this.totalProductionPercent = '+12.5%';
    this.activeFarmers = 45;
    this.activeFarmersParticipation = '85%';
    this.gradeAProduction = '98.2 MT';
    this.gradeAPercent = '78.3%';
    this.thisMonthDeliveries = '23.4 MT';
    this.thisMonthChange = '+8.2%';
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
    if (!this.user.cooperativeId) {
      console.warn('Cooperative ID not available, skipping production load');
      // Set empty arrays to prevent errors
      this.productionRecords = [];
      this.filteredProduction = [];
      this.paginatedProduction = [];
      this.totalPages = 1;
      return;
    }

    const params: any = {
      page: this.currentPage - 1,
      size: this.pageSize
    };

    if (this.selectedCrop !== 'All Crops') {
      params.productName = this.selectedCrop;
    }
    if (this.selectedGrade !== 'All Grades') {
      params.qualityGrade = this.selectedGrade;
    }
    if (this.searchQuery) {
      params.searchTerm = this.searchQuery;
    }

    this.productionService.getProductionRecords(
      this.user.cooperativeId,
      params.page,
      params.size,
      params.productName,
      params.qualityGrade,
      params.searchTerm
    ).subscribe({
      next: (response: any) => {
        // Handle API response structure
        let records: any[] = [];
        if (response.data && response.data.content) {
          records = response.data.content;
        } else if (response.content) {
          records = response.content;
        } else if (Array.isArray(response)) {
          records = response;
        }

        // Transform records to include farmer names and other display data
        this.productionRecords = records.map((record: any) => ({
          ...record,
          farmerName: `Farmer ${record.farmerId}`, // TODO: Fetch actual farmer name
          warehouse: record.notes || 'N/A',
          status: record.maturityStatus || 'PENDING'
        }));

        // Apply client-side filtering for display
        let filteredRecords = this.productionRecords;
        if (this.selectedCrop !== 'All Crops') {
          filteredRecords = filteredRecords.filter(record => record.productName === this.selectedCrop);
        }
        if (this.selectedGrade !== 'All Grades') {
          filteredRecords = filteredRecords.filter(record => record.qualityGrade === this.selectedGrade);
        }
        if (this.searchQuery) {
          filteredRecords = filteredRecords.filter(record =>
            (record.farmerName && record.farmerName.toLowerCase().includes(this.searchQuery.toLowerCase())) ||
            (record.id && record.id.toString().includes(this.searchQuery))
          );
        }

        this.filteredProduction = filteredRecords.map((record: ExtendedProductionRecord) =>
          this.transformRecord(record)
        );

        // Update pagination
        if (response.data && response.data.totalPages !== undefined) {
          this.totalPages = response.data.totalPages;
        } else if (response.totalPages !== undefined) {
          this.totalPages = response.totalPages;
        } else {
          this.totalPages = Math.ceil(this.filteredProduction.length / this.pageSize);
        }

        this.updatePaginatedData();
      },
      error: (error: any) => {
        console.error('Error loading productions:', error);
        // Fallback to empty array on error
        this.productionRecords = [];
        this.filteredProduction = [];
        this.paginatedProduction = [];
        this.totalPages = 1;
      }
    });
  }

  transformRecord(record: any): any {
    return {
      id: record.id,
      originalRecord: record, // Keep reference to original record
      date: record.productionDate ? new Date(record.productionDate).toLocaleDateString('en-GB') : 'N/A',
      farmer: record.farmerName || `Farmer ${record.farmerId}`,
      crop: this.getCropDisplayName(record.productName),
      quantity: typeof record.quantity === 'number' ? record.quantity : parseFloat(record.quantity || '0'),
      unit: record.unit || 'MT',
      grade: this.getGradeDisplayName(record.qualityGrade || 'GRADE_C'),
      gradeClass: this.getGradeClass(record.qualityGrade || 'GRADE_C'),
      warehouse: record.warehouse || record.notes || 'N/A',
      unitPrice: typeof record.unitPrice === 'number' ? record.unitPrice : parseFloat(record.unitPrice || '0'),
      valueXaf: typeof record.valueXaf === 'number' ? record.valueXaf : parseFloat(record.valueXaf || '0'),
      value: this.formatValue(typeof record.valueXaf === 'number' ? record.valueXaf : parseFloat(record.valueXaf || '0')),
      status: this.getStatusDisplayName(record.status || record.maturityStatus || 'PENDING'),
      statusClass: this.getStatusClass(record.status || record.maturityStatus || 'PENDING'),
      productName: record.productName,
      qualityGrade: record.qualityGrade,
      maturityStatus: record.maturityStatus,
      productionDate: record.productionDate
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
    // Use static warehouse data for now
    // TODO: Implement real warehouses API call
    this.warehouses = ['Douala Warehouse', 'Yaoundé Warehouse', 'Garoua Warehouse'];
    this.newProduction.warehouse = this.warehouses[0];
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

    if (!this.user.cooperativeId) {
      alert('Cooperative ID is not available. Please refresh the page and try again.');
      return;
    }

    // Ensure price is calculated
    this.calculatePrice();

    // Backend now accepts String IDs (e.g., "F51070", "C41069")
    console.log('Submitting production with data:', {
      farmerId: this.newProduction.farmerId,
      cooperativeId: this.user.cooperativeId,
      productName: this.newProduction.productName,
      quantity: this.newProduction.quantity,
      unitPrice: this.newProduction.unitPrice,
      valueXaf: this.newProduction.valueXaf
    });

    if (!this.newProduction.farmerId) {
      alert('Invalid farmer ID. Please select a farmer again.');
      console.error('Invalid farmerId:', this.newProduction.farmerId);
      return;
    }

    if (!this.user.cooperativeId) {
      alert('Invalid cooperative ID. Please refresh the page and try again.');
      console.error('Invalid cooperativeId:', this.user.cooperativeId);
      return;
    }

    const request: any = {
      farmerId: this.newProduction.farmerId, // Send as String
      cooperativeId: this.user.cooperativeId, // Send as String
      productType: 'CROP',
      productName: this.newProduction.productName,
      quantity: this.newProduction.quantity,
      unit: this.newProduction.unit,
      qualityGrade: this.newProduction.qualityGrade,
      maturityStatus: 'HARVESTED', // Valid values: IMMATURE, MATURE, READY_FOR_HARVEST, HARVESTED
      productionDate: this.newProduction.productionDate,
      unitPrice: this.newProduction.unitPrice,
      valueXaf: this.newProduction.valueXaf,
      notes: this.newProduction.warehouse || ''
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
        const errorMessage = error.error?.message || error.message || 'Please try again.';
        alert('Failed to record production: ' + errorMessage);
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.newProduction.farmerId &&
      this.newProduction.productName &&
      this.newProduction.quantity > 0 &&
      this.newProduction.unit &&
      this.newProduction.qualityGrade &&
      this.newProduction.maturityStatus &&
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
      unit: 'MT',
      qualityGrade: 'GRADE_A',
      maturityStatus: 'HARVESTED', // Add this line
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

  // View production record
  viewProduction(record: any): void {
    this.selectedRecord = record;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedRecord = null;
  }

  // Edit production record
  editProduction(record: any): void {
    const originalRecord = record.originalRecord || this.productionRecords.find(r => r.id === record.id);
    if (!originalRecord) {
      alert('Record not found');
      return;
    }

    this.editingRecord = {
      id: originalRecord.id,
      farmerId: originalRecord.farmerId,
      productName: originalRecord.productName,
      quantity: typeof originalRecord.quantity === 'number' ? originalRecord.quantity : parseFloat(originalRecord.quantity || '0'),
      unit: originalRecord.unit || 'MT',
      qualityGrade: originalRecord.qualityGrade || 'GRADE_A',
       maturityStatus: originalRecord.maturityStatus || 'HARVESTED',
      warehouse: originalRecord.warehouse || originalRecord.notes || this.warehouses[0],
      productionDate: originalRecord.productionDate ? originalRecord.productionDate.split('T')[0] : new Date().toISOString().split('T')[0],
      unitPrice: typeof originalRecord.unitPrice === 'number' ? originalRecord.unitPrice : parseFloat(originalRecord.unitPrice || '0'),
      valueXaf: typeof originalRecord.valueXaf === 'number' ? originalRecord.valueXaf : parseFloat(originalRecord.valueXaf || '0')
    };
    this.calculateEditPrice();
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingRecord = null;
  }

  calculateEditPrice(): void {
    if (!this.editingRecord) return;

    const { productName, qualityGrade, quantity } = this.editingRecord;

    if (!productName || !qualityGrade || !quantity || quantity <= 0) {
      this.editingRecord.unitPrice = 0;
      this.editingRecord.valueXaf = 0;
      return;
    }

    const unitPrice = this.priceConfig[productName]?.[qualityGrade] || 0;
    this.editingRecord.unitPrice = unitPrice;
    this.editingRecord.valueXaf = Math.round(unitPrice * quantity);
  }

  onEditFormChange(): void {
    this.calculateEditPrice();
  }

  isEditFormValid(): boolean {
    return !!(
      this.editingRecord &&
      this.editingRecord.productName &&
      this.editingRecord.quantity > 0 &&
      this.editingRecord.unit &&
      this.editingRecord.qualityGrade &&
      this.editingRecord.maturityStatus &&
      this.editingRecord.warehouse &&
      this.editingRecord.productionDate &&
      this.editingRecord.valueXaf > 0
    );
  }

  submitEdit(): void {
    if (!this.isEditFormValid() || !this.editingRecord) {
      alert('Please fill in all required fields');
      return;
    }

    this.calculateEditPrice();

    // Backend now accepts String IDs
    if (!this.editingRecord.farmerId || !this.user.cooperativeId) {
      alert('Invalid farmer or cooperative ID. Please try again.');
      return;
    }

    const request: any = {
      farmerId: this.editingRecord.farmerId.toString(), // Send as String
      cooperativeId: this.user.cooperativeId, // Send as String
      productType: 'CROP',
      productName: this.editingRecord.productName,
      quantity: this.editingRecord.quantity,
      unit: this.editingRecord.unit,
      qualityGrade: this.editingRecord.qualityGrade,
      maturityStatus: 'HARVESTED', // Valid values: IMMATURE, MATURE, READY_FOR_HARVEST, HARVESTED
      productionDate: this.editingRecord.productionDate,
      unitPrice: this.editingRecord.unitPrice,
      valueXaf: this.editingRecord.valueXaf,
      notes: this.editingRecord.warehouse || ''
    };

    this.productionService.updateProductionRecord(this.editingRecord.id.toString(), request).subscribe({
      next: (response: ProductionRecord) => {
        console.log('Production updated:', response);
        this.closeEditModal();
        this.loadProductions();
        this.loadDashboardMetrics();
        alert('Production updated successfully!');
      },
      error: (error: any) => {
        console.error('Error updating production:', error);
        alert('Failed to update production: ' + (error.message || 'Please try again.'));
      }
    });
  }

  // Delete production record
  deleteProduction(record: any): void {
    if (!confirm(`Are you sure you want to delete production record ${record.id}? This action cannot be undone.`)) {
      return;
    }

    const recordId = record.originalRecord?.id || record.id;
    if (!recordId) {
      alert('Record ID not found');
      return;
    }

    this.productionService.deleteProductionRecord(recordId.toString()).subscribe({
      next: () => {
        console.log('Production deleted:', recordId);
        this.loadProductions();
        this.loadDashboardMetrics();
        alert('Production record deleted successfully!');
      },
      error: (error: any) => {
        console.error('Error deleting production:', error);
        alert('Failed to delete production: ' + (error.message || 'Please try again.'));
      }
    });
  }

  exportProduction(): void {
    console.log('Export production data');
    // TODO: Implement export functionality
    alert('Export functionality will be implemented soon');
  }

  addNewProduction(): void {
    console.log('Add new production');
    this.onRecordProduction();
  }

  // ==================== SALES & MARKET METHODS ====================

  openNewProductModal(): void {
    // Implementation for opening new product listing modal
    alert('New Product Listing modal would open here');
  }

  viewProduct(product: ProductListing): void {
    this.selectedProduct = product;
    this.showViewProductModal = true;
  }

  editProduct(product: ProductListing): void {
    this.selectedProduct = product;
    this.showEditProductModal = true;
  }

  deleteProduct(product: ProductListing): void {
    if (confirm(`Are you sure you want to delete the listing for ${product.productName}?`)) {
      const index = this.productListings.findIndex(p => p.id === product.id);
      if (index > -1) {
        this.productListings.splice(index, 1);
        alert('Product listing deleted successfully!');
      }
    }
  }

  getProductStatusColor(status: string): string {
    switch (status) {
      case 'sold': return 'status-success';
      case 'listed': return 'status-primary';
      case 'pending': return 'status-warning';
      case 'cancelled': return 'status-danger';
      default: return 'status-default';
    }
  }

  getTrendIcon(trend: string): string {
    switch (trend) {
      case 'up': return '↗️';
      case 'down': return '↘️';
      default: return '→';
    }
  }

  formatCurrency(amount: number): string {
    return amount.toLocaleString('fr-FR') + ' XAF';
  }
}
