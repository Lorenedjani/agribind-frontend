import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

interface CropByRegion {
  region: string;
  farmerCount: number;
  totalQuantity: number;
  crops: Array<{ cropName: string; quantity: number; unit: string; farmerCount: number }>;
}

interface CropByFarmer {
  farmerName: string;
  farmerRegion: string;
  totalValueXaf: number;
  totalQuantity: number;
  deliveryCount: number;
  crops: Array<{ cropName: string; qualityGrade: string }>;
}

@Component({
  selector: 'app-cooperative-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {

  // User state
  currentUser: any;
  isCoopManager = false;
  coopId = '';
  userRegion = '';

  // Form state
  selectedRegion = 'All Regions';
  selectedPreset = 'THIS_MONTH';
  startDate = '';
  endDate = '';
  exportFormat = 'pdf'; // pdf | excel

  // Analytics data
  cropsByRegion: CropByRegion[] = [];
  cropsByFarmer: CropByFarmer[] = [];
  isLoading = false;
  
  // Simple chart data for visualization
  regionChartData: { label: string; value: number; percentage: number }[] = [];
  farmerChartData: { label: string; value: number }[] = [];
  
  // Computed properties for template
  get totalValue(): number {
    return this.cropsByFarmer.reduce((sum, f) => sum + f.totalValueXaf, 0);
  }
  
  get formattedTotalValue(): string {
    return this.formatNumber(this.totalValue);
  }
  
  getFarmValue(farm: CropByFarmer): string {
    return this.formatNumber(farm.totalValueXaf);
  }

  // Validation and UI state
  dateError = '';
  isGenerating = false;

  private readonly API_URL = '/api/v1/production';

  readonly presets = [
    { value: 'THIS_MONTH', label: 'This Month' },
    { value: 'LAST_MONTH', label: 'Last Month' },
    { value: 'LAST_3_MONTHS', label: 'Last 3 Months' },
    { value: 'THIS_YEAR', label: 'This Year' },
    { value: 'CUSTOM', label: 'Custom Range' }
  ];

  readonly allRegions = [
    'All Regions', 'Adamaoua', 'Centre', 'Est', 'Extrême-Nord',
    'Littoral', 'Nord', 'Nord-Ouest', 'Ouest', 'Sud', 'Sud-Ouest'
  ];

  constructor(
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    console.log('Current user in reports:', this.currentUser);

    // Normalize role — backend may return COOPERATIVE or COOPERATIVE_MANAGER
    const role = this.currentUser?.role || '';
    this.isCoopManager = ['COOPERATIVE', 'COOPERATIVE_MANAGER'].includes(role);

    if (this.isCoopManager) {
      this.coopId =
        this.currentUser?.cooperativeId ||
        this.currentUser?.cooperativeDetails?.cooperativeId ||
        '';

      // For COOPERATIVE users, show all regions but filter by cooperative
      // They can see farmers from different regions within their cooperative
      this.userRegion = this.currentUser?.region || this.currentUser?.cooperativeDetails?.region || 'Centre';
      this.selectedRegion = 'All Regions'; // Start with all regions, but scoped to cooperative
      
      console.log('Cooperative ID:', this.coopId);
      console.log('User Base Region:', this.userRegion);
      console.log('Selected Region:', this.selectedRegion);
    } else {
      // For GOVERNMENT users, show all regions and all cooperatives
      this.selectedRegion = 'All Regions';
    }

    this.setPreset('THIS_MONTH');
    this.loadAllData();
  }

  onFiltersChange() {
    this.loadAllData();
  }

  private loadAllData() {
    if (!this.startDate || !this.endDate) return;
    
    this.isLoading = true;
    
    // Build query params
    const params = new URLSearchParams();
    params.set('startDate', this.startDate);
    params.set('endDate', this.endDate);
    if (this.isCoopManager && this.coopId) {
      params.set('cooperativeId', this.coopId);
    }
    
    // Load both analytics endpoints
    const regionParams = `?${params.toString()}`;
    const farmerParams = `?${params.toString()}&region=${encodeURIComponent(this.selectedRegion)}`;
    
    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}) 
    });

    // Load crops by region
    this.http.get<any>(`${this.API_URL}/analytics/crops-by-region${regionParams}`, { headers })
      .subscribe({
        next: (res) => {
          this.cropsByRegion = res.data || [];
          this.prepareRegionChartData();
          this.checkLoadingComplete();
        },
        error: (err) => {
          console.error('Failed to load crops by region:', err);
          this.cropsByRegion = [];
          this.regionChartData = [];
          this.checkLoadingComplete();
        }
      });

    // Load crops by farmer
    this.http.get<any>(`${this.API_URL}/analytics/crops-by-farmer${farmerParams}`, { headers })
      .subscribe({
        next: (res) => {
          this.cropsByFarmer = res.data || [];
          this.prepareFarmerChartData();
          this.checkLoadingComplete();
        },
        error: (err) => {
          console.error('Failed to load crops by farmer:', err);
          this.cropsByFarmer = [];
          this.farmerChartData = [];
          this.checkLoadingComplete();
        }
      });
  }

  private checkLoadingComplete() {
    setTimeout(() => {
      this.isLoading = false;
    }, 500);
  }

  private prepareRegionChartData() {
    this.regionChartData = this.cropsByRegion.map(region => ({
      label: region.region,
      value: region.totalQuantity,
      percentage: Math.round((region.totalQuantity / this.cropsByRegion.reduce((sum, r) => sum + r.totalQuantity, 0)) * 100)
    }));
  }

  private prepareFarmerChartData() {
    this.farmerChartData = this.cropsByFarmer.slice(0, 10).map(farmer => ({
      label: farmer.farmerName,
      value: farmer.totalQuantity
    }));
    
    // Ensure we have at least one item for chart calculations
    if (this.farmerChartData.length === 0) {
      this.farmerChartData = [{ label: 'No Data', value: 0 }];
    }
  }

  getAvailableRegions(): string[] {
    if (this.isCoopManager) {
      // Cooperative managers can filter by regions but see all regions
      // The backend will filter by their cooperative ID
      return this.allRegions;
    }
    return this.allRegions;
  }

  setPreset(preset: string) {
    this.selectedPreset = preset;
    const today = new Date();
    let start = new Date();
    let end = new Date();

    switch(preset) {
      case 'THIS_MONTH':
        start = new Date(today.getFullYear(), today.getMonth(), 1);
        end = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        break;
      case 'LAST_MONTH':
        start = new Date(today.getFullYear(), today.getMonth() - 1, 1);
        end = new Date(today.getFullYear(), today.getMonth(), 0);
        break;
      case 'LAST_3_MONTHS':
        start = new Date(today.getFullYear(), today.getMonth() - 3, 1);
        break;
      case 'THIS_YEAR':
        start = new Date(today.getFullYear(), 0, 1);
        end = new Date(today.getFullYear(), 11, 31);
        break;
      case 'CUSTOM':
        // Leave dates as is
        return;
    }

    this.startDate = start.toISOString().split('T')[0];
    this.endDate = end.toISOString().split('T')[0];
    this.validateDates();
  }

  validateDates() {
    this.dateError = '';

    if (!this.startDate || !this.endDate) {
      this.dateError = 'Please select both start and end dates.';
      return;
    }

    const start = new Date(this.startDate);
    const end = new Date(this.endDate);

    if (start > end) {
      this.dateError = 'Start date cannot be after end date.';
    }
  }

  generateReport() {
    if (this.dateError || !this.startDate || !this.endDate) {
      alert(this.dateError || 'Please select valid dates.');
      return;
    }

    this.isGenerating = true;

    // Build params — cooperativeId only sent for coop manager (government has no scope restriction)
    const rawParams: Record<string, string> = {
      region: this.selectedRegion,
      startDate: this.startDate,
      endDate: this.endDate,
      format: this.exportFormat
    };
    if (this.isCoopManager && this.coopId) {
      rawParams['cooperativeId'] = this.coopId;
    }
    const params = new URLSearchParams(rawParams);
    const url = `${this.API_URL}/reports/farmer-production?${params.toString()}`;

    const token = this.authService.getToken();
    const headers = new HttpHeaders({ 
      Authorization: `Bearer ${token}`,
      Accept: this.exportFormat === 'excel' 
        ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        : 'application/pdf'
    });

    this.http.get(url, {
      responseType: 'blob',
      headers
    }).subscribe({
      next: (blob) => {
        this.isGenerating = false;
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        const ext = this.exportFormat === 'excel' ? 'xlsx' : 'pdf';
        link.download = `Farmer_Production_Report_${this.startDate}_to_${this.endDate}.${ext}`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);
      },
      error: (err) => {
        this.isGenerating = false;
        console.error('Report Generation Error:', err);
        alert('Failed to generate report. Please try again.');
      }
    });
  }

  formatNumber(value: number): string {
    return value?.toLocaleString('fr-FR') || '0';
  }
}
