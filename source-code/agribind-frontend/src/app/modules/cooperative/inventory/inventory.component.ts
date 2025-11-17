import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { InventoryService } from './inventory.service';
import { InventoryDashboardComponent } from './inventory-dashboard/inventory-dashboard.component';
import { CooperativeSidebarComponent } from '../../../../shared/cooperative-sidebar/cooperative-sidebar.component';

// Complete interface that matches your screenshot data
interface InventoryItem {
  id: string;
  name: string;
  category: string;
  quantity: number;
  minQuantity: number;
  value: number;
  location: string;
  status: string;
  trend: "up" | "down" | "flat";
  supplier?: string;
  lastUpdated?: Date;
  type: 'farmer' | 'input' | 'livestock';
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InventoryDashboardComponent,
    CooperativeSidebarComponent
  ],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent implements OnInit {

  items: InventoryItem[] = [];
  filteredItems: InventoryItem[] = [];
  categoryList: string[] = [];

  // Mock data based on your screenshot
  inventoryStats = {
    totalValue: 425000000,
    valueChange: 5.7,
    farmerProducts: 287.5,
    productsChange: 15.2,
    inputSupplies: 156,
    activeItems: 82,
    criticalStock: 18
  };

  searchQuery = '';
  selectedCategory = 'All Categories';
  activeTab: 'farmer' | 'input' | 'livestock' = 'farmer';

  // Pagination
  currentPage = 1;
  itemsPerPage = 5;
Math: any;

  constructor(private svc: InventoryService) {}

  ngOnInit(): void {
    // Use mock data that matches the screenshot exactly
    this.items = this.getMockData();
    this.applyFilters();
    this.categoryList = ['All Categories', 'Cocoa', 'Coffee', 'Palm Oil', 'Cotton', 'Cassava', 'Maize'];
  }

  getMockData(): InventoryItem[] {
    return [
      {
        id: 'PRO001',
        name: 'Cocoa Beans (Grade A)',
        category: 'Cocoa',
        quantity: 168.3,
        minQuantity: 50,
        value: 353430000,
        location: 'Douala Warehouse',
        status: 'In Stock',
        trend: 'up',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      {
        id: 'PRO002',
        name: 'Coffee Beans (Arabica A)',
        category: 'Coffee',
        quantity: 82.5,
        minQuantity: 30,
        value: 181500000,
        location: 'Yaoundé Warehouse',
        status: 'In Stock',
        trend: 'up',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      {
        id: 'PRO003',
        name: 'Palm Oil (Processed)',
        category: 'Palm Oil',
        quantity: 15.2,
        minQuantity: 25,
        value: 24320000,
        location: 'Douala Warehouse',
        status: 'Low Stock',
        trend: 'up',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      {
        id: 'PRO004',
        name: 'Cotton (Raw Grade B)',
        category: 'Cotton',
        quantity: 95.8,
        minQuantity: 40,
        value: 134120000,
        location: 'Garoua Warehouse',
        status: 'In Stock',
        trend: 'down',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      {
        id: 'PRO005',
        name: 'Cassava Flour (Grade A)',
        category: 'Cassava',
        quantity: 42.3,
        minQuantity: 20,
        value: 21150000,
        location: 'Yaoundé Warehouse',
        status: 'In Stock',
        trend: 'up',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      {
        id: 'PRO006',
        name: 'Maize (Yellow)',
        category: 'Maize',
        quantity: 8.5,
        minQuantity: 30,
        value: 4250000,
        location: 'Garoua Warehouse',
        status: 'Critical',
        trend: 'up',
        supplier: 'Farmers Cooperative',
        lastUpdated: new Date(),
        type: 'farmer'
      },
      // Input Supplies examples
      {
        id: 'INP001',
        name: 'Organic Fertilizer',
        category: 'Fertilizers',
        quantity: 120,
        minQuantity: 50,
        value: 3600000,
        location: 'Central Depot',
        status: 'In Stock',
        trend: 'up',
        supplier: 'AgroSupply Co.',
        lastUpdated: new Date(),
        type: 'input'
      },
      // Livestock examples
      {
        id: 'LIV001',
        name: 'Cattle (Beef)',
        category: 'Livestock',
        quantity: 45,
        minQuantity: 20,
        value: 22500000,
        location: 'Northern Ranch',
        status: 'In Stock',
        trend: 'up',
        supplier: 'Livestock Coop',
        lastUpdated: new Date(),
        type: 'livestock'
      }
    ];
  }

  switchTab(tab: 'farmer' | 'input' | 'livestock') {
    this.activeTab = tab;
    this.currentPage = 1;
    this.applyFilters();
  }

  applyFilters() {
    let filtered = [...this.items];

    // Filter by tab
    filtered = filtered.filter(x => x.type === this.activeTab);

    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      filtered = filtered.filter(x =>
        x.name.toLowerCase().includes(q) ||
        x.category.toLowerCase().includes(q) ||
        x.id.toLowerCase().includes(q)
      );
    }

    if (this.selectedCategory !== 'All Categories') {
      filtered = filtered.filter(x => x.category === this.selectedCategory);
    }

    this.filteredItems = filtered;
  }

  clearSearch() {
    this.searchQuery = '';
    this.applyFilters();
  }

  // Pagination methods
  get paginatedItems() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredItems.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.filteredItems.length / this.itemsPerPage);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const total = this.totalPages;

    if (total <= 5) {
      for (let i = 1; i <= total; i++) pages.push(i);
    } else {
      if (this.currentPage <= 3) {
        for (let i = 1; i <= 4; i++) pages.push(i);
        pages.push(-1); // Ellipsis
        pages.push(total);
      } else if (this.currentPage >= total - 2) {
        pages.push(1);
        pages.push(-1); // Ellipsis
        for (let i = total - 3; i <= total; i++) pages.push(i);
      } else {
        pages.push(1);
        pages.push(-1); // Ellipsis
        pages.push(this.currentPage - 1);
        pages.push(this.currentPage);
        pages.push(this.currentPage + 1);
        pages.push(-1); // Ellipsis
        pages.push(total);
      }
    }

    return pages;
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'in stock': return 'status-in-stock';
      case 'low stock': return 'status-low-stock';
      case 'critical': return 'status-critical';
      default: return 'status-in-stock';
    }
  }

  getTrendIcon(trend: "up" | "down" | "flat"): string {
    switch (trend) {
      case 'up': return '✔';
      case 'down': return '✘';
      case 'flat': return '→';
      default: return '→';
    }
  }

  formatNumber(num: number): string {
    return num.toLocaleString();
  }

  formatValue(value: number): string {
    return `${this.formatNumber(value)} XAF`;
  }

  formatQuantity(item: InventoryItem): string {
    return `${item.quantity} MT`;
  }

  formatMinQuantity(item: InventoryItem): string {
    return `Min ${item.minQuantity} MT`;
  }
}