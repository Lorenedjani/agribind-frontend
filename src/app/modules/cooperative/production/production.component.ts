import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';

interface ProductionRecord {
  id: string;
  date: string;
  farmer: string;
  crop: string;
  quantity: number;
  grade: string;
  warehouse: string;
  value: number;
  status: string;
  gradeClass: string;
  statusClass: string;
}

@Component({
  selector: 'app-record-production-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- TEMPLATE UNCHANGED (VALID) -->
    <div class="modal-backdrop" (click)="onCancel()" tabindex="-1">
      <div class="modal-content" (click)="$event.stopPropagation()">
        <header>
          <h2>Record Production</h2>
          <button class="close-btn" (click)="onCancel()" aria-label="Close">×</button>
        </header>

        <form #productionForm="ngForm" (ngSubmit)="onSubmit(productionForm)">
          <div class="form-group">
            <label for="farmer">Farmer Name</label>
            <input id="farmer" name="farmer" required [(ngModel)]="formData.farmer" />
          </div>

          <div class="form-group">
            <label for="crop">Crop Type</label>
            <select id="crop" name="crop" required [(ngModel)]="formData.crop" (change)="onCropChange()">
              <option value="" disabled>Select crop</option>
              <option *ngFor="let crop of cropTypes" [value]="crop">{{ crop }}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="quantity">Quantity (MT)</label>
            <input id="quantity" name="quantity" type="number" step="0.01" min="0" required
                   [(ngModel)]="formData.quantity" (input)="calculateValue()" />
          </div>

          <div class="form-group">
            <label for="grade">Grade</label>
            <select id="grade" name="grade" required [(ngModel)]="formData.grade">
              <option value="" disabled>Select grade</option>
              <option *ngFor="let grade of gradeOptions" [value]="grade">{{ grade }}</option>
            </select>
          </div>

          <div class="form-group">
            <label for="warehouse">Warehouse</label>
            <input id="warehouse" name="warehouse" required [(ngModel)]="formData.warehouse" />
          </div>

          <div class="form-group">
            <label>Price per MT (XAF)</label>
            <input type="number" [value]="pricePerMT" disabled />
          </div>

          <div class="form-group">
            <label for="value">Value (XAF)</label>
            <input id="value" name="value" [value]="formData.value" disabled />
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" [disabled]="!productionForm.valid">
              Submit
            </button>
            <button type="button" class="btn btn-outline" (click)="onCancel()">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    /* styles unchanged */
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.3);
      display: flex;
      justify-content: center;
      align-items: center;
      backdrop-filter: blur(3px);
      z-index: 2000;
    }
    .modal-content {
      background: #fff;
      padding: 24px 32px;
      border-radius: 12px;
      width: 400px;
    }
    header { display: flex; justify-content: space-between; align-items: center; }
    .close-btn { background: none; border: none; font-size: 24px; cursor: pointer; }
  `]
})
export class RecordProductionModalComponent {
  @Output() close = new EventEmitter<void>();
  @Output() submitRecord = new EventEmitter<ProductionRecord>();

  cropTypes = ['Cocoa', 'Coffee', 'Maize', 'Palm Oil', 'Cotton', 'Cassava'];
  gradeOptions = ['Grade A', 'Grade B', 'Grade C'];

  cropPrices: { [key: string]: number } = {
    Cocoa: 2100000,
    Coffee: 2200000,
    Maize: 500000,
    'Palm Oil': 1600000,
    Cotton: 1300000,
    Cassava: 500000,
  };

  formData: Partial<ProductionRecord> = {
    farmer: '',
    crop: '',
    quantity: 0,
    grade: '',
    warehouse: '',
    value: 0,
  };

  pricePerMT = 0;

  onCropChange() {
    const crop = this.formData.crop ?? '';
    this.pricePerMT = this.cropPrices[crop] || 0;
    this.calculateValue();
  }

  calculateValue() {
    const q = Number(this.formData.quantity) || 0;
    this.formData.value = q * this.pricePerMT;
  }

  onCancel() {
    this.close.emit();
  }

  onSubmit(form: NgForm) {
    if (form.valid) {
      const record: ProductionRecord = {
        id: `PROD${Date.now()}`,
        date: new Date().toLocaleDateString(),
        farmer: (this.formData.farmer ?? '').trim(),
        crop: this.formData.crop ?? '',
        quantity: Number(this.formData.quantity),
        grade: this.formData.grade ?? '',
        warehouse: (this.formData.warehouse ?? '').trim(),
        value: Number(this.formData.value),
        status: 'Pending',
        gradeClass: this.getGradeClass(this.formData.grade),
        statusClass: 'pending',
      };

      this.submitRecord.emit(record);
      this.onCancel();
    }
  }

  getGradeClass(grade: string | undefined): string {
    switch ((grade ?? '').toLowerCase()) {
      case 'grade a': return 'grade-a';
      case 'grade b': return 'grade-b';
      case 'grade c': return 'grade-c';
      default: return '';
    }
  }
}

export class ProductionComponent {
}
