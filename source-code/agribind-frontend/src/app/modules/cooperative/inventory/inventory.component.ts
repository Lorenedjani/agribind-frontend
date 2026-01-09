import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { InventoryService } from './inventory.service';
import { InventoryItem, InventorySummary } from './inventory.model';
import { AuthService } from '../../../core/services/auth.service';

interface DashboardMetrics {
  totalInventoryValue: string;
  totalInventoryPercent: string;
  totalItems: number;
  totalItemsPercent: string;
  lowStockItems: number;
  lowStockPercent: string;
  outOfStockItems: number;
  outOfStockPercent: string;
  criticalItems: number;
  criticalPercent: string;
  farmerProductsStock: string;
  farmerProductsPercent: string;
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

interface ResourceRequest {
  id: string;
  resourceType: string;
  resourceName: string;
  quantity: number;
  unit: string;
  urgency: 'low' | 'medium' | 'high' | 'critical';
  status: 'pending' | 'approved' | 'fulfilled' | 'rejected' | 'cancelled';
  requestDate: string;
  requiredDate: string;
  requesterName: string;
  description: string;
  supplierName?: string;
  fulfillmentDate?: string;
  estimatedCost: number;
  actualCost?: number;
}

interface Supplier {
  id: string;
  name: string;
  location: string;
  rating: number;
  contact: string;
  specialties: string[];
  responseTime: string;
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss'],
  providers: [InventoryService]
})

export class InventoryComponent implements OnInit {

  // Section state getters
  get isInventorySectionActive(): boolean {
    const inventoryBtn = this.actionButtons.find(btn => btn.name === 'Inventory');
    return inventoryBtn ? inventoryBtn.active : false;
  }

  get isSalesMarketSectionActive(): boolean {
    const salesBtn = this.actionButtons.find(btn => btn.name === 'Sales & Market');
    return salesBtn ? salesBtn.active : false;
  }

  get isResourcesSectionActive(): boolean {
    const resourcesBtn = this.actionButtons.find(btn => btn.name === 'Resources');
    return resourcesBtn ? resourcesBtn.active : false;
  }
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
      name: 'Inventory',
      icon: 'fas fa-boxes',
      active: true
    },
    {
      name: 'Sales & Market',
      icon: 'fas fa-shopping-cart',
      active: false
    },
    {
      name: 'Resources',
      icon: 'fas fa-tools',
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
    }
  ];

  // Resources Data
  suppliers: Supplier[] = [
    {
      id: 'SUP-001',
      name: 'AgriTech Supplies Ltd',
      location: 'Douala, Cameroon',
      rating: 4.8,
      contact: '+237 6XX XXX XXX',
      specialties: ['Fertilizers', 'Seeds', 'Equipment'],
      responseTime: '2-3 days'
    },
    {
      id: 'SUP-002',
      name: 'Green Valley Seeds',
      location: 'Yaoundé, Cameroon',
      rating: 4.6,
      contact: '+237 6XX XXX XXX',
      specialties: ['Seeds', 'Pesticides'],
      responseTime: '1-2 days'
    }
  ];

  resourceRequests: ResourceRequest[] = [
    {
      id: 'REQ-001',
      resourceType: 'Fertilizer',
      resourceName: 'NPK 20-10-10 Fertilizer',
      quantity: 500,
      unit: 'KG',
      urgency: 'high',
      status: 'pending',
      requestDate: '2025-01-10',
      requiredDate: '2025-01-20',
      requesterName: 'Jean Baptiste',
      description: 'Needed for cocoa plantation fertilization',
      estimatedCost: 750000
    },
    {
      id: 'REQ-002',
      resourceType: 'Seeds',
      resourceName: 'Hybrid Maize Seeds',
      quantity: 200,
      unit: 'KG',
      urgency: 'medium',
      status: 'approved',
      requestDate: '2025-01-08',
      requiredDate: '2025-01-25',
      requesterName: 'Marie Kouam',
      description: 'For maize planting season',
      supplierName: 'Green Valley Seeds',
      estimatedCost: 400000
    }
  ];

  // Modal states for sales and resources
  showNewProductModal = false;
  showViewProductModal = false;
  showEditProductModal = false;
  showNewResourceRequestModal = false;
  showViewResourceModal = false;
  showEditResourceModal = false;
  selectedProduct: ProductListing | null = null;
  selectedResourceRequest: ResourceRequest | null = null;

  // Dashboard metrics
  totalInventoryValue = '0 XAF';
  totalInventoryPercent = '0%';
  totalItems = 0;
  totalItemsPercent = '0%';
  lowStockItems = 0;
  lowStockPercent = '0%';
  outOfStockItems = 0;
  outOfStockPercent = '0%';
  criticalItems = 0;
  criticalPercent = '0%';
  farmerProductsStock = '0 MT';
  farmerProductsPercent = '0%';

  // Search and filters
  searchQuery = '';
  selectedCategory = 'all';
  selectedStatus = 'all';
  activeTab: 'products' | 'inputs' | 'livestock' = 'products';

  // Data
  allItems: InventoryItem[] = [];
  filteredItems: InventoryItem[] = [];
  summary: InventorySummary | null = null;

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 1;

  // Modal states
  showAddModal = false;
  showEditModal = false;
  showViewModal = false;
  currentItem: InventoryItem | null = null;
  addModalTab: 'products' | 'inputs' | 'livestock' = 'products';

  // Form data
  formData: InventoryItem = this.getEmptyFormData();

  // Loading state
  isLoading = false;
  errorMessage = '';

  // Categories
  categories = [
    { value: 'all', label: 'All Categories' },
    { value: 'Cocoa', label: 'Cocoa' },
    { value: 'Coffee', label: 'Coffee' },
    { value: 'Cotton', label: 'Cotton' },
    { value: 'Palm Oil', label: 'Palm Oil' },
    { value: 'Cassava', label: 'Cassava' },
    { value: 'Maize', label: 'Maize' },
    { value: 'Seeds', label: 'Seeds & Seedlings' },
    { value: 'Fertilizers', label: 'Fertilizers' },
    { value: 'Equipment', label: 'Equipment & Tools' },
    { value: 'Livestock Feed', label: 'Livestock Feed' },
    { value: 'Cattle', label: 'Cattle' },
    { value: 'Goats', label: 'Goats' },
    { value: 'Poultry', label: 'Poultry' },
    { value: 'Pigs', label: 'Pigs' },
    { value: 'Sheep', label: 'Sheep' }
  ];

  // Statuses
  statuses = [
    { value: 'all', label: 'All Status' },
    { value: 'IN_STOCK', label: 'In Stock' },
    { value: 'LOW_STOCK', label: 'Low Stock' },
    { value: 'CRITICAL', label: 'Critical' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' }
  ];

  // Locations
  locations = [
    'Douala Warehouse',
    'Yaoundé Warehouse',
    'Garoua Warehouse',
    'Central Depot',
    'Yaoundé Depot',
    'Garoua Depot',
    'Ranch A - Adamawa',
    'Farm B - North Region',
    'Poultry Farm C - West',
    'Farm D - Centre',
    'Ranch E - Northwest'
  ];

  constructor(
    private inventoryService: InventoryService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadInventoryData();
    this.loadSummary();
  }

  loadCurrentUser(): void {
    const currentUser = this.authService.getCurrentUser();

    if (currentUser) {
      let cooperativeId = currentUser.cooperativeId;
      if (!cooperativeId && currentUser.role === 'COOPERATIVE') {
        cooperativeId = currentUser.userId;
      }

      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        cooperativeId: cooperativeId || ''
      };
    }
  }

  // Handle action button clicks
  onActionButtonClick(buttonName: string): void {
    this.actionButtons.forEach(btn => btn.active = false);
    const clickedButton = this.actionButtons.find(btn => btn.name === buttonName);
    if (clickedButton) {
      clickedButton.active = true;
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

  loadInventoryData(): void {
    this.isLoading = true;
    this.inventoryService.getAllInventory().subscribe({
      next: (items) => {
        this.allItems = items;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load inventory data';
        this.isLoading = false;
        console.error('Error loading inventory:', error);
      }
    });
  }

  loadSummary(): void {
    this.inventoryService.getSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.updateDashboardMetrics(summary);
      },
      error: (error) => {
        console.error('Error loading summary:', error);
        this.loadMockMetrics();
      }
    });
  }

  updateDashboardMetrics(summary: InventorySummary): void {
    // Format total inventory value
    this.totalInventoryValue = this.formatValue(summary.totalInventoryValue);
    this.totalInventoryPercent = `+${summary.percentageChange.toFixed(1)}%`;

    // Total items
    this.totalItems = summary.totalItems;
    this.totalItemsPercent = '+5.2%'; // Mock percentage for now

    // Stock status metrics
    this.lowStockItems = summary.lowStockItems;
    this.lowStockPercent = `${((summary.lowStockItems / summary.totalItems) * 100).toFixed(1)}% of total`;

    this.outOfStockItems = summary.outOfStockItems;
    this.outOfStockPercent = `${((summary.outOfStockItems / summary.totalItems) * 100).toFixed(1)}% of total`;

    this.criticalItems = summary.criticalStockItems;
    this.criticalPercent = `${((summary.criticalStockItems / summary.totalItems) * 100).toFixed(1)}% of total`;

    // Farmer products stock
    this.farmerProductsStock = `${summary.farmerProductsStock.toFixed(1)} MT`;
    this.farmerProductsPercent = '+8.3%';
  }

  loadMockMetrics(): void {
    this.totalInventoryValue = '45,250,000 XAF';
    this.totalInventoryPercent = '+12.5%';
    this.totalItems = 245;
    this.totalItemsPercent = '+5.2%';
    this.lowStockItems = 18;
    this.lowStockPercent = '7.3% of total';
    this.outOfStockItems = 5;
    this.outOfStockPercent = '2.0% of total';
    this.criticalItems = 12;
    this.criticalPercent = '4.9% of total';
    this.farmerProductsStock = '156.8 MT';
    this.farmerProductsPercent = '+8.3%';
  }

  applyFilters(): void {
    let filtered = [...this.allItems];

    // Filter by tab type
    filtered = filtered.filter(item => this.matchesTab(item));

    // Filter by search
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(item =>
        item.itemName.toLowerCase().includes(query) ||
        item.itemId.toLowerCase().includes(query) ||
        item.category.toLowerCase().includes(query)
      );
    }

    // Filter by category
    if (this.selectedCategory !== 'all') {
      filtered = filtered.filter(item => item.category === this.selectedCategory);
    }

    // Filter by status
    if (this.selectedStatus !== 'all') {
      filtered = filtered.filter(item => item.status === this.selectedStatus);
    }

    this.filteredItems = filtered;
    this.totalPages = Math.ceil(filtered.length / this.pageSize);
  }

  matchesTab(item: InventoryItem): boolean {
    if (this.activeTab === 'products') {
      return item.itemId.startsWith('PRO') || item.itemType === 'FARMER_PRODUCT';
    } else if (this.activeTab === 'inputs') {
      return item.itemId.startsWith('INP') || item.itemType === 'INPUT_SUPPLY';
    } else {
      return item.itemId.startsWith('LIV') || item.itemType === 'LIVESTOCK';
    }
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onCategoryChange(): void {
    this.applyFilters();
  }

  onStatusChange(): void {
    this.applyFilters();
  }

  switchTab(tab: 'products' | 'inputs' | 'livestock'): void {
    this.activeTab = tab;
    this.applyFilters();
  }

  // Pagination
  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  // Modal actions
  openAddModal(): void {
    this.formData = this.getEmptyFormData();
    this.addModalTab = 'products';
    this.showAddModal = true;
  }

  updateFormCategory(): void {
    // Update formData based on selected tab
    if (this.addModalTab === 'products') {
      this.formData.unit = 'MT';
      this.formData.itemId = 'PRO' + Date.now().toString().slice(-3);
    } else if (this.addModalTab === 'inputs') {
      this.formData.unit = 'units';
      this.formData.itemId = 'INP' + Date.now().toString().slice(-3);
    } else {
      this.formData.unit = 'heads';
      this.formData.itemId = 'LIV' + Date.now().toString().slice(-3);
    }
  }

  onProductSelect(): void {
    // Map product name to category
    const productCategoryMap: { [key: string]: string } = {
      'Cocoa Beans': 'Cocoa',
      'Coffee Beans': 'Coffee',
      'Cotton': 'Cotton',
      'Palm Oil': 'Palm Oil',
      'Cassava': 'Cassava',
      'Maize': 'Maize'
    };

    if (this.formData.itemName) {
      this.formData.category = productCategoryMap[this.formData.itemName] || this.formData.itemName;
    }
  }

  openEditModal(item: InventoryItem): void {
    this.currentItem = item;
    this.formData = { ...item };
    this.showEditModal = true;
  }

  openViewModal(item: InventoryItem): void {
    this.currentItem = item;
    this.showViewModal = true;
  }

  closeModals(): void {
    this.showAddModal = false;
    this.showEditModal = false;
    this.showViewModal = false;
    this.currentItem = null;
    this.errorMessage = '';
  }

  onSaveAdd(): void {
    if (!this.validateForm()) return;

    this.isLoading = true;
    this.inventoryService.createInventoryItem(this.formData).subscribe({
      next: (item) => {
        this.loadInventoryData();
        this.loadSummary();
        this.closeModals();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to create item';
        this.isLoading = false;
        console.error('Error creating item:', error);
      }
    });
  }

  onSaveEdit(): void {
    if (!this.validateForm() || !this.currentItem?.id) return;

    this.isLoading = true;
    this.inventoryService.updateInventoryItem(this.currentItem.id, this.formData).subscribe({
      next: (item) => {
        this.loadInventoryData();
        this.loadSummary();
        this.closeModals();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to update item';
        this.isLoading = false;
        console.error('Error updating item:', error);
      }
    });
  }

  onDelete(item: InventoryItem): void {
    if (!item.id) return;

    if (confirm(`Are you sure you want to delete ${item.itemName}?`)) {
      this.inventoryService.deleteInventoryItem(item.id).subscribe({
        next: () => {
          this.loadInventoryData();
          this.loadSummary();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete item';
          console.error('Error deleting item:', error);
        }
      });
    }
  }

  validateForm(): boolean {
    if (!this.formData.itemId || !this.formData.itemName || !this.formData.category) {
      this.errorMessage = 'Please fill in all required fields';
      return false;
    }
    if (this.formData.quantity < 0 || this.formData.minimumQuantity < 0) {
      this.errorMessage = 'Quantities must be positive numbers';
      return false;
    }
    return true;
  }

  exportInventory(): void {
    console.log('Export inventory data');
    // TODO: Implement export functionality
    alert('Export functionality will be implemented soon');
  }

  addNewItem(): void {
    console.log('Add new inventory item');
    this.openAddModal();
  }

  getEmptyFormData(): InventoryItem {
    return {
      itemId: '',
      itemName: '',
      category: '',
      quantity: 0,
      minimumQuantity: 0,
      unit: 'MT',
      valueXAF: 0,
      location: '',
      status: 'IN_STOCK',
      trend: 'STABLE'
    };
  }

  onExport(): void {
    console.log('Export inventory data');
    // Implement export logic
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'IN_STOCK': return 'status-in-stock';
      case 'LOW_STOCK': return 'status-low-stock';
      case 'CRITICAL': return 'status-critical';
      case 'OUT_OF_STOCK': return 'status-out-of-stock';
      default: return 'status-in-stock';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'IN_STOCK': return 'In Stock';
      case 'LOW_STOCK': return 'Low Stock';
      case 'CRITICAL': return 'Critical';
      case 'OUT_OF_STOCK': return 'Out of Stock';
      default: return status;
    }
  }


  getTrendClass(trend: string): string {
    switch (trend) {
      case 'UP': return 'trend-up';
      case 'DOWN': return 'trend-down';
      case 'STABLE': return 'trend-stable';
      default: return 'trend-stable';
    }
  }

  formatValue(value: number): string {
    return value.toLocaleString('en-US');
  }

  formatQuantity(item: InventoryItem): string {
    return `${item.quantity} ${item.unit}`;
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

  // ==================== RESOURCES METHODS ====================

  openNewResourceRequestModal(): void {
    // Implementation for opening new resource request modal
    alert('New Resource Request modal would open here');
  }

  viewResourceRequest(request: ResourceRequest): void {
    this.selectedResourceRequest = request;
    this.showViewResourceModal = true;
  }

  editResourceRequest(request: ResourceRequest): void {
    this.selectedResourceRequest = request;
    this.showEditResourceModal = true;
  }

  deleteResourceRequest(request: ResourceRequest): void {
    if (confirm(`Are you sure you want to delete the request for ${request.resourceName}?`)) {
      const index = this.resourceRequests.findIndex(r => r.id === request.id);
      if (index > -1) {
        this.resourceRequests.splice(index, 1);
        alert('Resource request deleted successfully!');
      }
    }
  }

  getUrgencyColor(urgency: string): string {
    switch (urgency) {
      case 'critical': return 'urgency-critical';
      case 'high': return 'urgency-high';
      case 'medium': return 'urgency-medium';
      case 'low': return 'urgency-low';
      default: return 'urgency-medium';
    }
  }

  getRequestStatusColor(status: string): string {
    switch (status) {
      case 'fulfilled': return 'status-success';
      case 'approved': return 'status-success';
      case 'pending': return 'status-warning';
      case 'rejected': return 'status-danger';
      case 'cancelled': return 'status-default';
      default: return 'status-default';
    }
  }
}
