import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductionRecord {
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

export interface ProductionDashboardMetrics {
  totalProduction: string;
  totalProductionPercent: string;
  activeFarmers: number;
  activeFarmersParticipation: string;
  gradeAProduction: string;
  gradeAPercent: string;
  thisMonthDeliveries: string;
  thisMonthChange: string;
}

export interface CreateProductionRequest {
  farmerId: string;
  cropType: string;
  quantity: number;
  qualityGrade: string;
  warehouse: string;
  deliveryDate: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductionService {
  private apiUrl = '/api/production';

  constructor(private http: HttpClient) {}

  getDashboardMetrics(): Observable<ProductionDashboardMetrics> {
    return this.http.get<ProductionDashboardMetrics>(`${this.apiUrl}/dashboard`);
  }

  getProductions(params: any = {}): Observable<PageResponse<ProductionRecord>> {
    let httpParams = new HttpParams();

    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null) {
        httpParams = httpParams.set(key, params[key].toString());
      }
    });

    return this.http.get<PageResponse<ProductionRecord>>(this.apiUrl, { params: httpParams });
  }

  createProduction(request: CreateProductionRequest): Observable<ProductionRecord> {
    return this.http.post<ProductionRecord>(this.apiUrl, request);
  }

  getAvailableCropTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/crops`);
  }

  getQualityGrades(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/grades`);
  }

  getWarehouses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/warehouses`);
  }

  // Helper methods for display
  getCropDisplayName(cropType: string): string {
    const cropMap: { [key: string]: string } = {
      'COCOA': 'Cocoa',
      'COFFEE': 'Coffee',
      'MAIZE': 'Maize',
      'CASSAVA': 'Cassava',
      'RICE': 'Rice',
      'COTTON': 'Cotton'
    };
    return cropMap[cropType] || cropType;
  }

  getGradeDisplayName(grade: string): string {
    const gradeMap: { [key: string]: string } = {
      'GRADE_A': 'Grade A',
      'GRADE_B': 'Grade B',
      'GRADE_C': 'Grade C'
    };
    return gradeMap[grade] || grade;
  }

  getGradeClass(grade: string): string {
    const classMap: { [key: string]: string } = {
      'GRADE_A': 'grade-a',
      'GRADE_B': 'grade-b',
      'GRADE_C': 'grade-c'
    };
    return classMap[grade] || '';
  }

  getStatusClass(status: string): string {
    const classMap: { [key: string]: string } = {
      'PENDING': 'status-pending',
      'VERIFIED': 'status-verified',
      'REJECTED': 'status-rejected',
      'PROCESSED': 'status-processed',
      'SOLD': 'status-sold'
    };
    return classMap[status] || '';
  }

  formatValue(value: number): string {
    return `${value.toLocaleString()} XAF`;
  }
}