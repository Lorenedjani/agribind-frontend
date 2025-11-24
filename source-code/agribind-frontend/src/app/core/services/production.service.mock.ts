import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import {
  ProductionRecord,
  ProductionDashboardMetrics,
  CreateProductionRequest,
  PageResponse,
  ProductionService
} from './production.service';

@Injectable({
  providedIn: 'root'
})
export class MockProductionService extends ProductionService {
  private mockProductionRecords: ProductionRecord[] = [
    {
      productionId: 'PROD001',
      farmerId: '1',
      farmerName: 'Kwame Osei',
      cropType: 'COCOA',
      quantity: 45.2,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      deliveryDate: '2024-01-15',
      valueXaf: 9040000,
      status: 'VERIFIED'
    },
    {
      productionId: 'PROD002',
      farmerId: '2',
      farmerName: 'Ama Boateng',
      cropType: 'COFFEE',
      quantity: 32.7,
      qualityGrade: 'GRADE_B',
      warehouse: 'Yaoundé Warehouse',
      deliveryDate: '2024-01-18',
      valueXaf: 4905000,
      status: 'VERIFIED'
    },
    {
      productionId: 'PROD003',
      farmerId: '1',
      farmerName: 'Kwame Osei',
      cropType: 'COCOA',
      quantity: 38.9,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      deliveryDate: '2024-02-05',
      valueXaf: 7780000,
      status: 'PROCESSED'
    },
    {
      productionId: 'PROD004',
      farmerId: '3',
      farmerName: 'Yaw Mensah',
      cropType: 'COCOA',
      quantity: 52.1,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      deliveryDate: '2024-02-12',
      valueXaf: 10420000,
      status: 'SOLD'
    },
    {
      productionId: 'PROD005',
      farmerId: '4',
      farmerName: 'Akosua Darko',
      cropType: 'MAIZE',
      quantity: 28.4,
      qualityGrade: 'GRADE_C',
      warehouse: 'Garoua Warehouse',
      deliveryDate: '2024-02-20',
      valueXaf: 1420000,
      status: 'PENDING'
    },
    {
      productionId: 'PROD006',
      farmerId: '6',
      farmerName: 'Northwest Farmers Cooperative',
      cropType: 'COCOA',
      quantity: 125.8,
      qualityGrade: 'GRADE_A',
      warehouse: 'Douala Warehouse',
      deliveryDate: '2024-02-25',
      valueXaf: 25160000,
      status: 'VERIFIED'
    }
  ];

  private nextProductionId = 7;

  override getDashboardMetrics(): Observable<ProductionDashboardMetrics> {
    const metrics: ProductionDashboardMetrics = {
      totalProduction: '287.5 MT',
      totalProductionPercent: '+15.2% vs last cycle',
      activeFarmers: 245,
      activeFarmersParticipation: '81.7% participation rate',
      gradeAProduction: '168.3 MT',
      gradeAPercent: '58.5% premium quality',
      thisMonthDeliveries: '42.8 MT',
      thisMonthChange: '+8.3% vs last month'
    };

    return of(metrics).pipe(delay(400));
  }

  override getProductions(params: any = {}): Observable<PageResponse<ProductionRecord>> {
    let filteredRecords = [...this.mockProductionRecords];

    // Apply filters
    if (params.cropType) {
      filteredRecords = filteredRecords.filter(record => record.cropType === params.cropType);
    }
    if (params.qualityGrade) {
      filteredRecords = filteredRecords.filter(record => record.qualityGrade === params.qualityGrade);
    }
    if (params.searchTerm) {
      const search = params.searchTerm.toLowerCase();
      filteredRecords = filteredRecords.filter(record =>
        record.farmerName.toLowerCase().includes(search) ||
        record.warehouse.toLowerCase().includes(search)
      );
    }

    // Sort by delivery date (newest first)
    filteredRecords.sort((a, b) => new Date(b.deliveryDate).getTime() - new Date(a.deliveryDate).getTime());

    const page = params.page || 0;
    const size = params.size || 20;
    const start = page * size;
    const end = start + size;
    const paginatedRecords = filteredRecords.slice(start, end);

    const response: PageResponse<ProductionRecord> = {
      content: paginatedRecords,
      totalElements: filteredRecords.length,
      totalPages: Math.ceil(filteredRecords.length / size),
      currentPage: page,
      size: size
    };

    return of(response).pipe(delay(500));
  }

  override createProduction(request: CreateProductionRequest): Observable<ProductionRecord> {
    const newRecord: ProductionRecord = {
      productionId: `PROD${this.nextProductionId.toString().padStart(3, '0')}`,
      farmerId: request.farmerId,
      farmerName: this.getFarmerName(request.farmerId),
      cropType: request.cropType,
      quantity: request.quantity,
      qualityGrade: request.qualityGrade,
      warehouse: request.warehouse,
      deliveryDate: request.deliveryDate,
      valueXaf: this.calculateValue(request.cropType, request.quantity, request.qualityGrade),
      status: 'PENDING'
    };

    this.mockProductionRecords.push(newRecord);
    this.nextProductionId++;

    return of(newRecord).pipe(delay(600));
  }

  override getAvailableCropTypes(): Observable<string[]> {
    const crops = ['COCOA', 'COFFEE', 'MAIZE', 'CASSAVA', 'RICE', 'COTTON'];
    return of(crops).pipe(delay(300));
  }

  override getQualityGrades(): Observable<string[]> {
    const grades = ['GRADE_A', 'GRADE_B', 'GRADE_C'];
    return of(grades).pipe(delay(300));
  }

  override getWarehouses(): Observable<string[]> {
    const warehouses = ['Douala Warehouse', 'Yaoundé Warehouse', 'Garoua Warehouse', 'Bamenda Warehouse'];
    return of(warehouses).pipe(delay(300));
  }

  private getFarmerName(farmerId: string): string {
    const farmerNames: { [key: string]: string } = {
      '1': 'Kwame Osei',
      '2': 'Ama Boateng',
      '3': 'Yaw Mensah',
      '4': 'Akosua Darko',
      '5': 'Kofi Asante',
      '6': 'Northwest Farmers Cooperative',
      '7': 'South Region Cocoa Cooperative'
    };
    return farmerNames[farmerId] || 'Unknown Farmer';
  }

  private calculateValue(cropType: string, quantity: number, grade: string): number {
    const basePrices: { [key: string]: number } = {
      'COCOA': 200000,
      'COFFEE': 150000,
      'MAIZE': 50000,
      'CASSAVA': 30000,
      'RICE': 80000,
      'COTTON': 120000
    };

    const gradeMultipliers: { [key: string]: number } = {
      'GRADE_A': 1.0,
      'GRADE_B': 0.8,
      'GRADE_C': 0.6
    };

    const basePrice = basePrices[cropType] || 50000;
    const multiplier = gradeMultipliers[grade] || 0.7;

    return Math.round(quantity * basePrice * multiplier);
  }
}
