import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

export interface PriceData {
  commodity: string;
  icon: string;
  currentPrice: number;
  yourPrice: number | null;
  effectiveDate: Date | null;
  expirationDate: Date | null;
  status: 'Active' | 'Draft' | 'Expired';
  regions: string[];
  allRegions: boolean;
}

interface CommodityDef {
  name: string;
  icon: string;
  currentPrice: number;
  yourPrice: number | null;
  status: 'Active' | 'Draft' | 'Expired';
}

interface MarketPriceDto {
  commodityCode: string;
  commodityName: string;
  market: string;
  currency: string;
  price: number;
  updatedAt: string;
  version: number;
  checksum: string;
  priceSource: string;
}

@Component({
  selector: 'app-set-market-prices',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './set-market-prices.component.html',
  styleUrls: ['./set-market-prices.component.scss']
})
export class SetMarketPricesComponent implements OnInit {
  private readonly api = '/api/v1';

  // ── Form state ─────────────────────────────────────────────
  selectedCommodity = '';
  priceInput = '';
  effectiveDate = '';
  expirationDate = '';
  allRegions = true;
  selectedRegions: string[] = [];

  // ── New commodity creation ──────────────────────────────────
  isAddingNew      = false;
  newName          = '';
  newIcon          = '🌾';
  newBasePrice     = '';

  readonly iconOptions = [
    '🌾','🍌','🥜','🫘','🌿','🍅','🫚','🌰','🍠','🥦',
    '🌱','🌻','🍋','🍊','🥑','🍇','🥕','🧅','🌽','🫛'
  ];

  // ── Catalogue (mutable — new commodities are appended) ──────
  commodities: CommodityDef[] = [
    { name: 'Cocoa',    icon: '🍫', currentPrice: 1200, yourPrice: 1500, status: 'Active'  },
    { name: 'Coffee',   icon: '☕', currentPrice: 950,  yourPrice: 1100, status: 'Active'  },
    { name: 'Palm Oil', icon: '🌴', currentPrice: 800,  yourPrice: 850,  status: 'Draft'   },
    { name: 'Cotton',   icon: '🌿', currentPrice: 650,  yourPrice: 650,  status: 'Active'  },
    { name: 'Cassava',  icon: '🥔', currentPrice: 200,  yourPrice: 220,  status: 'Active'  },
    { name: 'Maize',    icon: '🌽', currentPrice: 180,  yourPrice: null, status: 'Expired' }
  ];

  regions = [
    'Centre', 'Littoral', 'West', 'South-West', 'North-West',
    'Adamawa', 'North', 'Far North', 'East', 'South'
  ];

  priceData:      PriceData[] = [];
  loadError:      string | null = null;
  successMessage: string | null = null;
  saving = false;

  constructor(private http: HttpClient, private authService: AuthService) {}

  ngOnInit(): void {
    this.initPriceBoardFromLocal();
    this.loadAdminPrices();
  }

  // ── HTTP ────────────────────────────────────────────────────

  private authHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    });
  }

  private initPriceBoardFromLocal(): void {
    const now          = new Date();
    const inThirtyDays = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000);
    this.priceData = this.commodities.map(c => ({
      commodity:      c.name,
      icon:           c.icon,
      currentPrice:   c.currentPrice,
      yourPrice:      c.yourPrice,
      effectiveDate:  c.yourPrice ? now : null,
      expirationDate: c.yourPrice ? inThirtyDays : null,
      status:         c.status,
      regions:        [],
      allRegions:     true
    }));
  }

  private loadAdminPrices(): void {
    this.loadError = null;
    this.http.get<MarketPriceDto[]>(`${this.api}/market/admin/prices`, { headers: this.authHeaders() })
      .subscribe({
        next:  rows => this.applyServerRows(rows),
        error: ()   => { this.loadError = 'Could not load prices from server (using local defaults).'; }
      });
  }

  /**
   * Merge server rows into priceData.
   * Commodities returned from the server that are NOT in the local list
   * (e.g. created in a previous session) are dynamically added so they
   * survive a page reload.
   */
  private applyServerRows(rows: MarketPriceDto[]): void {
    for (const row of rows) {
      const name = this.nameForCode(row.commodityCode);
      if (!this.priceData.find(p => p.commodity === name)) {
        this.appendCommodity(name, '🌾', 0);
      }
      const item = this.priceData.find(p => p.commodity === name);
      if (!item) continue;
      if (row.priceSource === 'GOVERNMENT') {
        item.yourPrice = row.price;
        item.status    = 'Active';
        if (row.updatedAt) item.effectiveDate = new Date(row.updatedAt);
      } else if (row.priceSource === 'SYSTEM') {
        item.currentPrice = row.price;
      }
    }
  }

  // ── Code / name helpers ─────────────────────────────────────

  private codeFor(name: string): string {
    return name.trim().toUpperCase().replace(/\s+/g, '_');
  }

  private nameForCode(code: string): string {
    const map: Record<string, string> = {
      COCOA: 'Cocoa', COFFEE: 'Coffee', PALM_OIL: 'Palm Oil',
      COTTON: 'Cotton', CASSAVA: 'Cassava', MAIZE: 'Maize'
    };
    return map[code] ?? code.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }

  // ── New commodity flow ──────────────────────────────────────

  toggleAddNew(): void {
    this.isAddingNew = !this.isAddingNew;
    this.newName = ''; this.newIcon = '🌾'; this.newBasePrice = '';
  }

  pickIcon(icon: string): void { this.newIcon = icon; }

  confirmNewCommodity(): void {
    const name = this.newName.trim();
    if (!name) return;
    if (this.commodities.find(c => c.name.toLowerCase() === name.toLowerCase())) {
      this.loadError = `"${name}" already exists — select it from the list.`;
      setTimeout(() => this.loadError = null, 4000);
      return;
    }
    const base = this.newBasePrice ? parseFloat(this.newBasePrice) : 0;
    this.appendCommodity(name, this.newIcon, base);
    this.selectCommodity(name);          // auto-select so user can price it right away
    this.isAddingNew = false;
    this.newName = ''; this.newIcon = '🌾'; this.newBasePrice = '';
  }

  private appendCommodity(name: string, icon: string, basePrice: number): void {
    this.commodities = [...this.commodities, { name, icon, currentPrice: basePrice, yourPrice: null, status: 'Draft' }];
    this.priceData   = [...this.priceData,   {
      commodity: name, icon, currentPrice: basePrice, yourPrice: null,
      effectiveDate: null, expirationDate: null, status: 'Draft', regions: [], allRegions: true
    }];
  }

  // ── Commodity & region selection ────────────────────────────

  selectCommodity(name: string): void {
    this.selectedCommodity = name;
    const e = this.priceData.find(p => p.commodity === name);
    this.priceInput = e?.yourPrice != null ? e.yourPrice.toString() : '';
  }

  isRegionSelected(region: string): boolean { return this.selectedRegions.includes(region); }

  toggleRegion(region: string): void {
    const idx = this.selectedRegions.indexOf(region);
    this.selectedRegions = idx === -1
      ? [...this.selectedRegions, region]
      : this.selectedRegions.filter(r => r !== region);
  }

  // ── Price board actions ─────────────────────────────────────

  handlePriceUpdate(): void {
    if (!this.selectedCommodity || !this.priceInput) return;
    const newPrice = parseFloat(this.priceInput);
    const effDate  = this.effectiveDate  ? new Date(this.effectiveDate)  : null;
    const expDate  = this.expirationDate ? new Date(this.expirationDate) : null;
    this.priceData = this.priceData.map(item =>
      item.commodity === this.selectedCommodity
        ? { ...item, yourPrice: newPrice, effectiveDate: effDate, expirationDate: expDate,
            status: 'Draft' as const, allRegions: this.allRegions,
            regions: this.allRegions ? [] : [...this.selectedRegions] }
        : item
    );
    this.resetForm();
  }

  private resetForm(): void {
    this.selectedCommodity = ''; this.priceInput = '';
    this.effectiveDate = ''; this.expirationDate = '';
    this.selectedRegions = [];
  }

  private buildBody(item: PriceData): object {
    const market = item.allRegions || item.regions.length === 0 ? 'International' : item.regions[0];
    return {
      commodityCode: this.codeFor(item.commodity),
      commodityName: item.commodity,
      market,
      currency:      'XAF',
      price:         item.yourPrice!,
      effectiveFrom: this.toLocalDate(item.effectiveDate),
      effectiveTo:   this.toLocalDate(item.expirationDate),
      status:        'Active'
    };
  }

  publishPrice(commodity: string): void {
    const item = this.priceData.find(p => p.commodity === commodity);
    if (!item?.yourPrice) return;
    this.saving = true; this.loadError = null;
    this.http.post<MarketPriceDto>(`${this.api}/market/admin/prices`, this.buildBody(item), { headers: this.authHeaders() })
      .subscribe({
        next: () => {
          this.priceData = this.priceData.map(i => i.commodity === commodity ? { ...i, status: 'Active' as const } : i);
          this.saving = false;
          this.successMessage = `${commodity} price published! Farmers will see the updated price in the mobile app.`;
          setTimeout(() => this.successMessage = null, 6000);
        },
        error: () => { this.saving = false; this.loadError = `Failed to publish ${commodity}. Check API gateway.`; }
      });
  }

  broadcastAllActive(): void {
    const drafts = this.priceData.filter(p => p.status === 'Draft' && p.yourPrice != null);
    if (!drafts.length) {
      this.successMessage = 'No draft prices to publish.';
      setTimeout(() => this.successMessage = null, 3000);
      return;
    }
    const publishNext = (rem: PriceData[]) => {
      if (!rem.length) {
        this.saving = false;
        this.successMessage = 'All draft prices published and broadcast to farmers!';
        setTimeout(() => this.successMessage = null, 6000);
        return;
      }
      const [head, ...tail] = rem;
      this.saving = true;
      this.http.post<MarketPriceDto>(`${this.api}/market/admin/prices`, this.buildBody(head), { headers: this.authHeaders() })
        .subscribe({
          next: () => {
            this.priceData = this.priceData.map(i => i.commodity === head.commodity ? { ...i, status: 'Active' as const } : i);
            publishNext(tail);
          },
          error: () => { this.saving = false; this.loadError = `Broadcast stopped: failed on ${head.commodity}.`; }
        });
    };
    publishNext(drafts);
  }

  // ── Display helpers ─────────────────────────────────────────

  editPrice(item: PriceData): void {
    this.selectedCommodity = item.commodity;
    this.priceInput        = item.yourPrice?.toString() ?? '';
    this.allRegions        = item.allRegions;
    this.selectedRegions   = [...item.regions];
    this.effectiveDate     = item.effectiveDate  ? this.toInputDateString(item.effectiveDate)  : '';
    this.expirationDate    = item.expirationDate ? this.toInputDateString(item.expirationDate) : '';
  }

  private toLocalDate(d: Date | null): string | null { return d ? d.toISOString().substring(0, 10) : null; }
  private toInputDateString(d: Date): string         { return d.toISOString().substring(0, 10); }

  getPriceChange(item: PriceData): number {
    if (!item.yourPrice || !item.currentPrice) return 0;
    return ((item.yourPrice - item.currentPrice) / item.currentPrice) * 100;
  }

  getPriceChangeClass(item: PriceData): string {
    const c = this.getPriceChange(item);
    return c > 0 ? 'positive' : c < 0 ? 'negative' : 'neutral';
  }

  getStatusClass(status: string): string {
    return ({ Active: 'status-active', Draft: 'status-draft', Expired: 'status-expired' } as Record<string,string>)[status] ?? '';
  }
}
