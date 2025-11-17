import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

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

@Injectable({
  providedIn: 'root'
})
export class ProductionService {
  private baseUrl = `${environment.apiUrl}/api/v1/production`;

  constructor(private http: HttpClient) {}

  // Create new production record
  createProduction(request: CreateProductionRequest): Observable<ProductionRecord> {
    return this.http.post<ProductionRecord>(this.baseUrl, request);
  }

  // Get production records with filters
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
    let httpParams = new HttpParams();

    Object.keys(params).forEach(key => {
      const value = params[key as keyof typeof params];
      if (value !== null && value !== undefined) {
        httpParams = httpParams.set(key, value.toString());
      }
    });

    return this.http.get<PageResponse<ProductionRecord>>(this.baseUrl, { params: httpParams });
  }

  // Get production by ID
  getProductionById(productionId: string): Observable<ProductionRecord> {
    return this.http.get<ProductionRecord>(`${this.baseUrl}/${productionId}`);
  }

  // Update production status
  updateProductionStatus(
    productionId: string,
    status: string,
    reason?: string
  ): Observable<ProductionRecord> {
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

  // Get dashboard metrics
  getDashboardMetrics(): Observable<ProductionDashboardMetrics> {
    return this.http.get<ProductionDashboardMetrics>(`${this.baseUrl}/dashboard/metrics`);
  }

  // Get available crop types
  getAvailableCropTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/crops`);
  }

  // Get quality grades
  getQualityGrades(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/grades`);
  }

  // Get production statuses
  getProductionStatuses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/statuses`);
  }

  // Get warehouses
  getWarehouses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/warehouses`);
  }

  // Format value for display
  formatValue(value: number): string {
    return new Intl.NumberFormat('fr-FR').format(value) + ' XAF';
  }

  // Format quantity for display
  formatQuantity(quantity: number): string {
    return `${quantity} MT`;
  }

  // Get grade class for styling
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

  // Get status class for styling
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

  // Get display name for crop type
  getCropDisplayName(cropType: string): string {
    return cropType.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, l => l.toUpperCase());
  }

  // Get display name for quality grade
  getGradeDisplayName(grade: string): string {
    return grade.replace(/_/g, ' ');
  }
}
