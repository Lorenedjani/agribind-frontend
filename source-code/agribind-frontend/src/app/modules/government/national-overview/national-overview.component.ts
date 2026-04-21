import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

interface RegionStat { region: string; cropCount: number; farmerCount: number; production: number; }
interface CooperativeSummary { id: string; name: string; region: string; farmerCount: number; status: string; }

@Component({
  selector: 'app-national-overview',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  template: `
    <div class="national-overview">
      <!-- Header -->
      <div class="page-header">
        <div class="header-left">
          <h1>🇨🇲 National Agricultural Overview</h1>
          <p class="subtitle">Real-time national statistics across all regions of Cameroon</p>
        </div>
        <button class="refresh-btn" (click)="loadData()" [disabled]="isLoading">
          <span *ngIf="!isLoading">🔄 Refresh</span>
          <span *ngIf="isLoading">⏳ Loading...</span>
        </button>
      </div>

      <!-- KPI Cards -->
      <div class="kpi-grid">
        <div class="kpi-card farmers">
          <div class="kpi-icon">👨‍🌾</div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.totalFarmers | number }}</span>
            <span class="kpi-label">Total Farmers</span>
          </div>
        </div>
        <div class="kpi-card cooperatives">
          <div class="kpi-icon">🏢</div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.totalCooperatives | number }}</span>
            <span class="kpi-label">Active Cooperatives</span>
          </div>
        </div>
        <div class="kpi-card crops">
          <div class="kpi-icon">🌾</div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.totalCropTypes }}</span>
            <span class="kpi-label">Distinct crop types (national)</span>
          </div>
        </div>
        <div class="kpi-card production">
          <div class="kpi-icon">📦</div>
          <div class="kpi-body">
            <span class="kpi-value">{{ stats.totalProduction | number }} MT</span>
            <span class="kpi-label">Total Production</span>
          </div>
        </div>
      </div>

      <!-- Region Stats Table -->
      <div class="section-card">
        <div class="section-header">
          <h2>📍 Statistics by Region</h2>
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Region</th>
                <th>Crop Count</th>
                <th>Farmers</th>
                <th>Production (MT)</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let stat of regionStats">
                <td><strong>{{ stat.region }}</strong></td>
                <td>{{ stat.cropCount }}</td>
                <td>{{ stat.farmerCount | number }}</td>
                <td>{{ stat.production | number }}</td>
                <td><span class="badge active">Active</span></td>
              </tr>
              <tr *ngIf="regionStats.length === 0 && !isLoading">
                <td colspan="5" class="empty-row">No regional data available</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Cooperatives Table -->
      <div class="section-card">
        <div class="section-header">
          <h2>🏢 Cooperatives Overview</h2>
          <input class="search-input" [(ngModel)]="searchTerm" placeholder="Search cooperatives..." (input)="filterCooperatives()" />
        </div>
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Region</th>
                <th>Farmers</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let coop of filteredCooperatives">
                <td><strong>{{ coop.name }}</strong></td>
                <td>{{ coop.region }}</td>
                <td>{{ coop.farmerCount }}</td>
                <td><span class="badge" [class.active]="coop.status === 'ACTIVE'" [class.inactive]="coop.status !== 'ACTIVE'">{{ coop.status }}</span></td>
              </tr>
              <tr *ngIf="filteredCooperatives.length === 0 && !isLoading">
                <td colspan="4" class="empty-row">No cooperatives found</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .national-overview { padding: 24px; display: flex; flex-direction: column; gap: 24px; }
    .page-header { display: flex; align-items: flex-start; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
    .page-header h1 { font-size: 24px; font-weight: 700; color: #1e3a5f; margin: 0; }
    .subtitle { font-size: 14px; color: #6b7280; margin: 4px 0 0; }
    .refresh-btn { background: #1e3a5f; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 500; transition: background 0.2s; }
    .refresh-btn:hover:not(:disabled) { background: #2d5a8e; }
    .refresh-btn:disabled { opacity: 0.6; cursor: not-allowed; }
    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; }
    .kpi-card { background: white; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.07); border-left: 4px solid #ccc; transition: transform 0.2s; }
    .kpi-card:hover { transform: translateY(-2px); }
    .kpi-card.farmers { border-left-color: #10b981; }
    .kpi-card.cooperatives { border-left-color: #3b82f6; }
    .kpi-card.crops { border-left-color: #f59e0b; }
    .kpi-card.production { border-left-color: #8b5cf6; }
    .kpi-icon { font-size: 32px; }
    .kpi-body { display: flex; flex-direction: column; }
    .kpi-value { font-size: 28px; font-weight: 700; color: #1e3a5f; }
    .kpi-label { font-size: 13px; color: #6b7280; }
    .section-card { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.07); overflow: hidden; }
    .section-header { padding: 16px 20px; border-bottom: 1px solid #f3f4f6; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
    .section-header h2 { font-size: 16px; font-weight: 600; color: #1e3a5f; margin: 0; }
    .search-input { border: 1px solid #d1d5db; border-radius: 8px; padding: 8px 12px; font-size: 14px; outline: none; }
    .search-input:focus { border-color: #3b82f6; }
    .table-wrapper { overflow-x: auto; }
    .data-table { width: 100%; border-collapse: collapse; font-size: 14px; }
    .data-table th { background: #f8fafc; padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 600; color: #6b7280; text-transform: uppercase; letter-spacing: 0.05em; }
    .data-table td { padding: 12px 16px; border-top: 1px solid #f3f4f6; color: #374151; }
    .data-table tr:hover td { background: #f8fafc; }
    .empty-row { text-align: center; color: #9ca3af; padding: 32px !important; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 99px; font-size: 12px; font-weight: 500; }
    .badge.active { background: #d1fae5; color: #065f46; }
    .badge.inactive { background: #fce7f3; color: #9d174d; }
  `]
})
export class NationalOverviewComponent implements OnInit {
  isLoading = false;
  searchTerm = '';

  stats = { totalFarmers: 0, totalCooperatives: 0, totalCropTypes: 0, totalProduction: 0 };
  regionStats: RegionStat[] = [];
  cooperatives: CooperativeSummary[] = [];
  filteredCooperatives: CooperativeSummary[] = [];

  private readonly userApi = environment.services.userManagement;
  private readonly productionApi = environment.services.productionApiUrl;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit() { this.loadData(); }

  private authHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    });
  }

  loadData() {
    this.isLoading = true;
    this.loadStats();
    this.loadCooperatives();
  }

  loadStats() {
    const end = new Date();
    const start = new Date(end.getFullYear(), 0, 1);
    const startStr = start.toISOString().split('T')[0];
    const endStr = end.toISOString().split('T')[0];
    const h = { headers: this.authHeaders() };

    forkJoin({
      metrics: this.http.get<any>(`${this.userApi}/dashboard/metrics`, h),
      crops: this.http.get<any>(
        `${this.productionApi}/analytics/crops-by-region?startDate=${startStr}&endDate=${endStr}`,
        h
      )
    }).subscribe({
      next: ({ metrics, crops }) => {
        this.stats.totalFarmers = Number(metrics.totalFarmers ?? 0);
        this.stats.totalCooperatives = Number(metrics.totalCooperatives ?? 0);
        const top = metrics.topCrops as Record<string, number> | undefined;
        this.stats.totalCropTypes = top ? Object.keys(top).length : 0;

        const regions = crops?.data ?? crops ?? [];
        let productionTotal = 0;
        this.regionStats = (regions as any[]).map((r: any) => {
          const qty = Number(r.totalQuantity ?? 0);
          productionTotal += qty;
          return {
            region: r.region ?? '—',
            cropCount: Number(r.cropTypeCount ?? r.crops?.length ?? 0),
            farmerCount: Number(r.farmerCount ?? 0),
            production: qty
          };
        });
        this.stats.totalProduction = productionTotal;
        this.isLoading = false;
      },
      error: () => {
        if (environment.demoMode) {
          this.stats = { totalFarmers: 1250, totalCooperatives: 47, totalCropTypes: 18, totalProduction: 32400 };
          this.regionStats = [
            { region: 'Centre', cropCount: 8, farmerCount: 245, production: 6800 },
            { region: 'Littoral', cropCount: 6, farmerCount: 198, production: 5200 },
            { region: 'West', cropCount: 9, farmerCount: 312, production: 8100 },
            { region: 'Southwest', cropCount: 7, farmerCount: 189, production: 4900 },
            { region: 'Far North', cropCount: 5, farmerCount: 306, production: 7400 },
          ];
        } else {
          this.stats = { totalFarmers: 0, totalCooperatives: 0, totalCropTypes: 0, totalProduction: 0 };
          this.regionStats = [];
        }
        this.isLoading = false;
      }
    });
  }

  loadCooperatives() {
    this.http.get<any>(`${this.userApi}/users?type=COOPERATIVE&size=100`, { headers: this.authHeaders() }).subscribe({
      next: (data) => {
        const items = data.content ?? data ?? [];
        this.cooperatives = items.map((c: any) => ({
          id: c.userId ?? c.id,
          name: c.name || c.cooperativeName || '—',
          region: c.region ?? c.operatingRegion ?? 'N/A',
          farmerCount: c.cooperativeDetails?.activeMemberCount ?? c.activeMemberCount ?? c.memberCount ?? 0,
          status: c.status ?? 'ACTIVE'
        }));
        this.filteredCooperatives = [...this.cooperatives];
      },
      error: () => {
        if (environment.demoMode) {
          this.cooperatives = [
            { id: '1', name: 'Coopagri Centre', region: 'Centre', farmerCount: 85, status: 'ACTIVE' },
            { id: '2', name: 'Agro-West Union', region: 'West', farmerCount: 120, status: 'ACTIVE' },
            { id: '3', name: 'Littoral Farmers Coop', region: 'Littoral', farmerCount: 67, status: 'ACTIVE' },
          ];
        } else {
          this.cooperatives = [];
        }
        this.filteredCooperatives = [...this.cooperatives];
      }
    });
  }

  filterCooperatives() {
    const term = this.searchTerm.toLowerCase();
    this.filteredCooperatives = this.cooperatives.filter(c =>
      c.name.toLowerCase().includes(term) || c.region.toLowerCase().includes(term)
    );
  }
}