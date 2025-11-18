// src/app/core/services/production.service.ts
// COPY THIS ENTIRE FILE TO: src/app/core/services/production.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

// ============================================
// TYPE DEFINITIONS - EXPORT ALL
// ============================================

export interface ProductionRecord {
  productionId: string;
  deliveryDate: string;
  farmerId: string;
  farmerName: string;
  cropType: string;
  quantity: number;
  qualityGrade: string;
  warehouse: string;
  valueXaf: number;
  status: string;
  notes?: string;
  createdAt: string;
  verifiedBy?: string;
  verificationDate?: string;
}

export interface CreateProductionRequest {
  farmerId: string;
  cropType: string;
  quantity: number;
  qualityGrade: string;
  warehouse: string;
  deliveryDate: string;
  notes?: string;
}

export interface ProductionDashboardMetrics {
  totalProduction: string;
  totalProductionPercent: string;
  activeFarmers: number;
  activeFarmersParticipation: string;
  gradeAProduction: string;
  gradeAPercent: string;
  thisMonthDeliveries: string;
  thisMonthChange: string;
  cropDistribution: { [key: string]: number };
  statusDistribution: { [key: string]: number };
  gradeDistribution: { [key: string]: number };
  warehouseDistribution: { [key: string]: number };
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

// ============================================
// SERVICE IMPLEMENTATION
// ============================================

@Injectable({
  providedIn: 'root'
})
export class ProductionService {
  private baseUrl = `${environment.apiUrl}/api/v1/production`;
  private useMockData = true; // Set to false when backend is ready

  // Mock data for development
  private mockProductions: ProductionRecord[] = [
    {
      productionId: 'PROD-001',
      deliveryDate: '2024-11-15',
      farmerId: 'M001',
      farmerName: 'Kwame Osei',
      cropType: 'COCOA',
      quantity: 2.5,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      valueXaf: 5250000,
      status: 'VERIFIED',
      createdAt: '2024-11-15T08:30:00',
      verifiedBy: 'Inspector John',
      verificationDate: '2024-11-15T10:00:00'
    },
    {
      productionId: 'PROD-002',
      deliveryDate: '2024-11-16',
      farmerId: 'M002',
      farmerName: 'Arna Boateng',
      cropType: 'COFFEE',
      quantity: 1.8,
      qualityGrade: 'GRADE_A',
      warehouse: 'Yaoundé Warehouse',
      valueXaf: 3960000,
      status: 'VERIFIED',
      createdAt: '2024-11-16T09:15:00'
    },
    {
      productionId: 'PROD-003',
      deliveryDate: '2024-11-16',
      farmerId: 'M003',
      farmerName: 'Yaw Mensah',
      cropType: 'COCOA',
      quantity: 3.2,
      qualityGrade: 'GRADE_B',
      warehouse: 'Douala Warehouse',
      valueXaf: 6080000,
      status: 'PENDING',
      createdAt: '2024-11-16T14:20:00'
    },
    {
      productionId: 'PROD-004',
      deliveryDate: '2024-11-17',
      farmerId: 'M004',
      farmerName: 'Akosua Darko',
      cropType: 'MAIZE',
      quantity: 5.0,
      qualityGrade: 'GRADE_A',
      warehouse: 'Garoua Warehouse',
      valueXaf: 2500000,
      status: 'VERIFIED',
      createdAt: '2024-11-17T07:45:00'
    },
    {
      productionId: 'PROD-005',
      deliveryDate: '2024-11-17',
      farmerId: 'M005',
      farmerName: 'Kofi Asante',
      cropType: 'COTTON',
      quantity: 4.5,
      qualityGrade: 'GRADE_B',
      warehouse: 'Garoua Warehouse',
      valueXaf: 4050000,
      status: 'VERIFIED',
      createdAt: '2024-11-17T11:30:00'
    },
    {
      productionId: 'PROD-006',
      deliveryDate: '2024-11-18',
      farmerId: 'M006',
      farmerName: 'Abena Owusu',
      cropType: 'COCOA',
      quantity: 2.8,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      valueXaf: 5880000,
      status: 'VERIFIED',
      createdAt: '2024-11-18T10:20:00'
    }
  ];

  constructor(private http: HttpClient) {
    console.log('ProductionService initialized');
    console.log('Using mock data:', this.useMockData);
  }

  // ============================================
  // PUBLIC METHODS
  // ============================================

  createProduction(request: CreateProductionRequest): Observable<ProductionRecord> {
    if (this.useMockData) {
      return this.mockCreateProduction(request);
    }
    return this.http.post<ProductionRecord>(this.baseUrl, request);
  }

  getProductions(params: {
    cropType?: string;
    qualityGrade?: string;
    status?: string;
    warehouse?: string;
    searchTerm?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: string;
  }): Observable<PageResponse<ProductionRecord>> {
    if (this.useMockData) {
      return this.mockGetProductions(params);
    }

    let httpParams = new HttpParams();
    Object.keys(params).forEach(key => {
      const value = params[key as keyof typeof params];
      if (value !== null && value !== undefined) {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    return this.http.get<PageResponse<ProductionRecord>>(this.baseUrl, { params: httpParams });
  }

  getProductionById(productionId: string): Observable<ProductionRecord> {
    if (this.useMockData) {
      const record = this.mockProductions.find(p => p.productionId === productionId);
      return of(record!).pipe(delay(200));
    }
    return this.http.get<ProductionRecord>(`${this.baseUrl}/${productionId}`);
  }

  updateProductionStatus(
    productionId: string,
    status: string,
    reason?: string
  ): Observable<ProductionRecord> {
    if (this.useMockData) {
      const record = this.mockProductions.find(p => p.productionId === productionId);
      if (record) {
        record.status = status;
        if (status === 'VERIFIED') {
          record.verifiedBy = 'Admin';
          record.verificationDate = new Date().toISOString();
        }
      }
      return of(record!).pipe(delay(300));
    }

    let httpParams = new HttpParams().set('status', status);
    if (reason) {
      httpParams = httpParams.set('reason', reason);
    }

    return this.http.patch<ProductionRecord>(
      `${this.baseUrl}/${productionId}/status`,
      null,
      { params: httpParams }
    );
  }

  getDashboardMetrics(): Observable<ProductionDashboardMetrics> {
    if (this.useMockData) {
      return this.mockGetDashboardMetrics();
    }
    return this.http.get<ProductionDashboardMetrics>(`${this.baseUrl}/dashboard/metrics`);
  }

  getAvailableCropTypes(): Observable<string[]> {
    if (this.useMockData) {
      const crops = ['COCOA', 'COFFEE', 'MAIZE', 'COTTON', 'CASSAVA', 'PALM_OIL'];
      return of(crops).pipe(delay(100));
    }
    return this.http.get<string[]>(`${this.baseUrl}/crops`);
  }

  getQualityGrades(): Observable<string[]> {
    if (this.useMockData) {
      const grades = ['GRADE_A', 'GRADE_B', 'GRADE_C'];
      return of(grades).pipe(delay(100));
    }
    return this.http.get<string[]>(`${this.baseUrl}/grades`);
  }

  getProductionStatuses(): Observable<string[]> {
    if (this.useMockData) {
      const statuses = ['PENDING', 'VERIFIED', 'REJECTED', 'PROCESSED', 'SOLD'];
      return of(statuses).pipe(delay(100));
    }
    return this.http.get<string[]>(`${this.baseUrl}/statuses`);
  }

  getWarehouses(): Observable<string[]> {
    if (this.useMockData) {
      const warehouses = [
        'Douala Warehouse',
        'Yaoundé Warehouse',
        'Garoua Warehouse',
        'Bamenda Warehouse',
        'Bafoussam Warehouse'
      ];
      return of(warehouses).pipe(delay(100));
    }
    return this.http.get<string[]>(`${this.baseUrl}/warehouses`);
  }

  // ============================================
  // UTILITY METHODS
  // ============================================

  formatValue(value: number): string {
    return new Intl.NumberFormat('fr-FR').format(value) + ' XAF';
  }

  formatQuantity(quantity: number): string {
    return `${quantity} MT`;
  }

  getGradeClass(grade: string): string {
    switch (grade) {
      case 'GRADE_A':
        return 'grade-a';
      case 'GRADE_B':
        return 'grade-b';
      case 'GRADE_C':
        return 'grade-c';
      default:
        return '';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'VERIFIED':
        return 'verified';
      case 'PENDING':
        return 'pending';
      case 'REJECTED':
        return 'rejected';
      case 'PROCESSED':
        return 'processed';
      case 'SOLD':
        return 'sold';
      default:
        return '';
    }
  }

  getCropDisplayName(cropType: string): string {
    return cropType.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  }

  getGradeDisplayName(grade: string): string {
    return grade.replace(/_/g, ' ');
  }

  // ============================================
  // MOCK DATA METHODS
  // ============================================

  private mockCreateProduction(request: CreateProductionRequest): Observable<ProductionRecord> {
    const newRecord: ProductionRecord = {
      productionId: `PROD-${String(this.mockProductions.length + 1).padStart(3, '0')}`,
      deliveryDate: request.deliveryDate,
      farmerId: request.farmerId,
      farmerName: this.getFarmerName(request.farmerId),
      cropType: request.cropType,
      quantity: request.quantity,
      qualityGrade: request.qualityGrade,
      warehouse: request.warehouse,
      valueXaf: this.calculateValue(request.cropType, request.quantity, request.qualityGrade),
      status: 'PENDING',
      notes: request.notes,
      createdAt: new Date().toISOString()
    };

    this.mockProductions.push(newRecord);
    console.log('Created production record:', newRecord);
    return of(newRecord).pipe(delay(500));
  }

  private mockGetProductions(params: any): Observable<PageResponse<ProductionRecord>> {
    let filtered = [...this.mockProductions];

    // Apply filters
    if (params.cropType) {
      filtered = filtered.filter(p => p.cropType === params.cropType);
    }
    if (params.qualityGrade) {
      filtered = filtered.filter(p => p.qualityGrade === params.qualityGrade);
    }
    if (params.status) {
      filtered = filtered.filter(p => p.status === params.status);
    }
    if (params.warehouse) {
      filtered = filtered.filter(p => p.warehouse === params.warehouse);
    }
    if (params.searchTerm) {
      const search = params.searchTerm.toLowerCase();
      filtered = filtered.filter(p =>
        p.productionId.toLowerCase().includes(search) ||
        p.farmerName.toLowerCase().includes(search) ||
        p.cropType.toLowerCase().includes(search)
      );
    }

    // Pagination
    const page = params.page || 0;
    const size = params.size || 20;
    const start = page * size;
    const end = start + size;
    const paginatedData = filtered.slice(start, end);

    const response: PageResponse<ProductionRecord> = {
      content: paginatedData,
      currentPage: page,
      pageSize: size,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size)
    };

    return of(response).pipe(delay(300));
  }

  private mockGetDashboardMetrics(): Observable<ProductionDashboardMetrics> {
    const totalQuantity = this.mockProductions.reduce((sum, p) => sum + p.quantity, 0);
    const gradeAProductions = this.mockProductions.filter(p => p.qualityGrade === 'GRADE_A');
    const gradeAQuantity = gradeAProductions.reduce((sum, p) => sum + p.quantity, 0);
    const uniqueFarmers = new Set(this.mockProductions.map(p => p.farmerId)).size;

    const metrics: ProductionDashboardMetrics = {
      totalProduction: `${totalQuantity.toFixed(1)} MT`,
      totalProductionPercent: '+15.2% vs last cycle',
      activeFarmers: uniqueFarmers,
      activeFarmersParticipation: '81.7% participation rate',
      gradeAProduction: `${gradeAQuantity.toFixed(1)} MT`,
      gradeAPercent: `${((gradeAQuantity / totalQuantity) * 100).toFixed(1)}% premium quality`,
      thisMonthDeliveries: `${totalQuantity.toFixed(1)} MT`,
      thisMonthChange: '+8.3% vs last month',
      cropDistribution: this.getCropDistribution(),
      statusDistribution: this.getStatusDistribution(),
      gradeDistribution: this.getGradeDistribution(),
      warehouseDistribution: this.getWarehouseDistribution()
    };

    return of(metrics).pipe(delay(200));
  }

  // Helper methods
  private getCropDistribution(): { [key: string]: number } {
    const dist: { [key: string]: number } = {};
    this.mockProductions.forEach(p => {
      dist[p.cropType] = (dist[p.cropType] || 0) + p.quantity;
    });
    return dist;
  }

  private getStatusDistribution(): { [key: string]: number } {
    const dist: { [key: string]: number } = {};
    this.mockProductions.forEach(p => {
      dist[p.status] = (dist[p.status] || 0) + 1;
    });
    return dist;
  }

  private getGradeDistribution(): { [key: string]: number } {
    const dist: { [key: string]: number } = {};
    this.mockProductions.forEach(p => {
      dist[p.qualityGrade] = (dist[p.qualityGrade] || 0) + p.quantity;
    });
    return dist;
  }

  private getWarehouseDistribution(): { [key: string]: number } {
    const dist: { [key: string]: number } = {};
    this.mockProductions.forEach(p => {
      dist[p.warehouse] = (dist[p.warehouse] || 0) + p.quantity;
    });
    return dist;
  }

  private getFarmerName(farmerId: string): string {
    const farmerMap: { [key: string]: string } = {
      'M001': 'Kwame Osei',
      'M002': 'Arna Boateng',
      'M003': 'Yaw Mensah',
      'M004': 'Akosua Darko',
      'M005': 'Kofi Asante',
      'M006': 'Abena Owusu',
      'M007': 'Kwabena Amoah',
      'M008': 'Efua Agyeman'
    };
    return farmerMap[farmerId] || 'Unknown Farmer';
  }

  private calculateValue(cropType: string, quantity: number, grade: string): number {
    const basePrice: { [key: string]: number } = {
      'COCOA': 2100000,
      'COFFEE': 2200000,
      'MAIZE': 500000,
      'COTTON': 900000,
      'CASSAVA': 400000,
      'PALM_OIL': 1600000
    };

    const gradeMultiplier: { [key: string]: number } = {
      'GRADE_A': 1.0,
      'GRADE_B': 0.85,
      'GRADE_C': 0.70
    };

    const base = basePrice[cropType] || 1000000;
    const multiplier = gradeMultiplier[grade] || 1.0;

    return Math.round(base * quantity * multiplier);
  }
}
