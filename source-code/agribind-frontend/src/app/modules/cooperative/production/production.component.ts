// src/app/modules/cooperative/production/production.component.ts
// DELETE ALL CONTENT AND REPLACE WITH THIS

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CooperativeSidebarComponent } from "../../../../shared/cooperative-sidebar/cooperative-sidebar.component";
import {
  ProductionService,
  ProductionRecord,
  ProductionDashboardMetrics,
  CreateProductionRequest,
  PageResponse
} from '../../../core/services/production.service';

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, FormsModule, CooperativeSidebarComponent],
  templateUrl: './production.component.html',
  styleUrls: ['./production.component.scss']
})
export class ProductionComponent implements OnInit {
  user = { name: 'Emmanuel Njoya', role: 'Manager', initials: 'EN' };

  // Dashboard metrics
  metrics: ProductionDashboardMetrics | null = null;
  totalProduction = '287.5 MT';
  totalProductionPercent = '+15.2% vs last cycle';
  activeFarmers = 245;
  activeFarmersParticipation = '81.7% participation rate';
  gradeAProduction = '168.3 MT';
  gradeAPercent = '58.5% premium quality';
  thisMonthDeliveries = '42.8 MT';
  thisMonthChange = '+8.3% vs last month';

  // Filter options
  searchQuery = '';
  selectedCrop = 'All Crops';
  selectedGrade = 'All Grades';
  cropTypes: string[] = ['All Crops'];
  qualityGrades: string[] = ['All Grades'];
  warehouses: string[] = [];
  farmers: string[] = [];

  // Production data
  productionRecords: ProductionRecord[] = [];
  filteredProduction: any[] = [];
  paginatedProduction: any[] = [];

  // Pagination
  currentPage = 1;
  pageSize = 20;
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
    warehouse: 'Douala Warehouse',
    deliveryDate: new Date().toISOString().split('T')[0]
  };

  // Export options
  exportFormat = 'csv';
  exportDateRange = 'all';
  exportStartDate = '';
  exportEndDate = '';

  constructor(private productionService: ProductionService) {}

  ngOnInit(): void {
    this.loadDashboardMetrics();
    this.loadProductions();
    this.loadCropTypes();
    this.loadQualityGrades();
    this.loadWarehouses();
    this.loadFarmers();
  }

  loadDashboardMetrics(): void {
    this.productionService.getDashboardMetrics().subscribe({
      next: (metrics: ProductionDashboardMetrics) => {
        this.metrics = metrics;
        this.totalProduction = metrics.totalProduction;
        this.totalProductionPercent = metrics.totalProductionPercent;
        this.activeFarmers = metrics.activeFarmers;
        this.activeFarmersParticipation = metrics.activeFarmersParticipation;
        this.gradeAProduction = metrics.gradeAProduction;
        this.gradeAPercent = metrics.gradeAPercent;
        this.thisMonthDeliveries = metrics.thisMonthDeliveries;
        this.thisMonthChange = metrics.thisMonthChange;
      },
      error: (error: any) => console.error('Error loading metrics:', error)
    });
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
      next: (response: PageResponse<ProductionRecord>) => {
        this.productionRecords = response.content;
        this.filteredProduction = response.content.map((record: ProductionRecord) => this.transformRecord(record));
        this.totalPages = response.totalPages;
        this.updatePaginatedData();
      },
      error: (error: any) => console.error('Error loading productions:', error)
    });
  }

  loadCropTypes(): void {
    this.productionService.getAvailableCropTypes().subscribe({
      next: (crops: string[]) => {
        this.cropTypes = ['All Crops', ...crops];
      },
      error: (error: any) => console.error('Error loading crop types:', error)
    });
  }

  loadQualityGrades(): void {
    this.productionService.getQualityGrades().subscribe({
      next: (grades: string[]) => {
        this.qualityGrades = ['All Grades', ...grades];
      },
      error: (error: any) => console.error('Error loading grades:', error)
    });
  }

  loadWarehouses(): void {
    this.productionService.getWarehouses().subscribe({
      next: (warehouses: string[]) => {
        this.warehouses = warehouses;
      },
      error: (error: any) => console.error('Error loading warehouses:', error)
    });
  }

  loadFarmers(): void {
    // This would typically come from the user service
    this.farmers = [
      'Kwame Osei (M001)',
      'Arna Boateng (M002)',
      'Yaw Mensah (M003)',
      'Akosua Darko (M004)',
      'Kofi Asante (M005)',
      'Abena Owusu (M006)',
      'Kwabena Amoah (M007)',
      'Efua Agyeman (M008)'
    ];
  }

  transformRecord(record: ProductionRecord): any {
    return {
      id: record.productionId,
      date: new Date(record.deliveryDate).toLocaleDateString('en-GB'),
      farmer: record.farmerName,
      crop: this.productionService.getCropDisplayName(record.cropType),
      quantity: record.quantity + ' MT',
      grade: this.productionService.getGradeDisplayName(record.qualityGrade),
      gradeClass: this.productionService.getGradeClass(record.qualityGrade),
      warehouse: record.warehouse,
      value: this.productionService.formatValue(record.valueXaf),
      status: this.getStatusDisplayName(record.status),
      statusClass: this.productionService.getStatusClass(record.status)
    };
  }

  getStatusDisplayName(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'Pending',
      'VERIFIED': 'Verified',
      'REJECTED': 'Rejected',
      'PROCESSED': 'Processed',
      'SOLD': 'Sold'
    };
    return statusMap[status] || status;
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
  }

  submitProduction(): void {
    if (!this.isFormValid()) {
      alert('Please fill in all required fields');
      return;
    }

    // Extract farmer ID from selection (format: "Name (ID)")
    const farmerMatch = this.newProduction.farmerId.match(/\(([^)]+)\)/);
    const farmerId = farmerMatch ? farmerMatch[1] : this.newProduction.farmerId;

    const request: CreateProductionRequest = {
      ...this.newProduction,
      farmerId: farmerId
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
        alert('Failed to record production. Please try again.');
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
      warehouse: 'Douala Warehouse',
      deliveryDate: new Date().toISOString().split('T')[0]
    };
  }

  onExport(): void {
    this.showExportModal = true;
  }

  closeExportModal(): void {
    this.showExportModal = false;
  }

  performExport(): void {
    console.log('Exporting data in format:', this.exportFormat);
    // Export functionality would be implemented here
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
