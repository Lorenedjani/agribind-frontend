import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
    <div class="modal-backdrop" (click)="onCancel()" tabindex="-1">
      <div class="modal-content" (click)="$event.stopPropagation()" role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <header>
          <h2 id="modal-title">Record Production</h2>
          <button class="close-btn" (click)="onCancel()" aria-label="Close modal">×</button>
        </header>

        <form #productionForm="ngForm" (ngSubmit)="onSubmit(productionForm)">
          <div class="form-group">
            <label for="farmer">Farmer Name</label>
            <input id="farmer" name="farmer" required [(ngModel)]="formData.farmer" placeholder="Enter farmer name" />
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
            <input id="quantity" name="quantity" type="number" step="0.01" min="0" required [(ngModel)]="formData.quantity" (input)="calculateValue()" placeholder="Enter quantity" />
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
            <input id="warehouse" name="warehouse" required [(ngModel)]="formData.warehouse" placeholder="Enter warehouse location" />
          </div>

          <div class="form-group">
            <label>Price per MT (XAF)</label>
            <input type="number" [value]="pricePerMT" disabled />
          </div>

          <div class="form-group">
            <label for="value">Value (XAF)</label>
            <input id="value" name="value" [value]="formData.value | number" disabled />
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" [disabled]="!productionForm.form.valid">Submit</button>
            <button type="button" class="btn btn-outline" (click)="onCancel()">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    /* Same styles as previously provided for modal: backdrop, content, buttons, etc. */
    .modal-backdrop {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
      backdrop-filter: blur(3px);
    }

    .modal-content {
      background: #fff;
      border-radius: 12px;
      width: 400px;
      max-width: 90vw;
      padding: 24px 32px;
      box-shadow: 0 8px 24px rgba(50, 128, 72, 0.2);
      display: flex;
      flex-direction: column;
      position: relative;
    }

    header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    header h2 {
      margin: 0;
      font-weight: 700;
      font-size: 22px;
      color: var(--color-sidebar-green);
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 28px;
      cursor: pointer;
      line-height: 1;
      color: #999;
      transition: color 0.3s;
    }

    .close-btn:hover {
      color: var(--color-sidebar-green);
    }

    form .form-group {
      display: flex;
      flex-direction: column;
      margin-bottom: 16px;
    }

    form label {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 6px;
      color: #444;
    }

    form input,
    form select {
      padding: 10px 14px;
      font-size: 14px;
      border: 1.5px solid #ccc;
      border-radius: 8px;
      transition: border-color 0.3s;
    }

    form input:focus,
    form select:focus {
      outline: none;
      border-color: var(--color-sidebar-green);
      box-shadow: 0 0 5px rgba(50,128,72,0.3);
    }

    .modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 20px;
    }

    .modal-actions .btn {
      min-width: 100px;
      border-radius: 24px;
      padding: 10px 18px;
      font-weight: 700;
      cursor: pointer;
      transition: background-color 0.3s;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }

    .modal-actions .btn-primary {
      background-color: var(--color-sidebar-green);
      color: white;
      border: none;
    }

    .modal-actions .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .modal-actions .btn-primary:hover:not(:disabled) {
      background-color: var(--color-sidebar-green-dark);
    }

    .modal-actions .btn-outline {
      background: white;
      color: var(--color-sidebar-green);
      border: 1.5px solid var(--color-sidebar-green);
    }

    .modal-actions .btn-outline:hover {
      background: #daf3d9;
      border-color: var(--color-sidebar-green-dark);
      color: var(--color-sidebar-green-dark);
    }
  `]
})
export class RecordProductionModalComponent {
  @Output() close = new EventEmitter<void>();
  @Output() submitRecord = new EventEmitter<ProductionRecord>();

  cropTypes = ['Cocoa', 'Coffee', 'Maize', 'Palm Oil', 'Cotton', 'Cassava'];
  gradeOptions = ['Grade A', 'Grade B', 'Grade C'];

  // Example prices per MT by crop type (adjust with realistic data)
  cropPrices: {[key: string]: number} = {
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
    quantity: null,
    grade: '',
    warehouse: '',
    value: null,
  };
  pricePerMT: number = 0;

  onCropChange() {
    this.pricePerMT = this.formData.crop ? this.cropPrices[this.formData.crop] || 0 : 0;
    this.calculateValue();
  }

  calculateValue() {
    const q = this.formData.quantity || 0;
    this.formData.value = q * this.pricePerMT;
  }

  onCancel() {
    this.close.emit();
  }

  onSubmit(form) {
    if (form.valid) {
      const record: ProductionRecord = {
        id: `PROD${Date.now()}`, // simplistic unique id
        date: new Date().toLocaleDateString(),
        farmer: this.formData.farmer.trim(),
        crop: this.formData.crop,
        quantity: this.formData.quantity,
        grade: this.formData.grade,
        warehouse: this.formData.warehouse.trim(),
        value: this.formData.value,
        status: 'Pending',
        gradeClass: this.getGradeClass(this.formData.grade),
        statusClass: 'pending',
      };
      this.submitRecord.emit(record);
      this.onCancel();
    }
  }

  getGradeClass(grade: string): string {
    switch((grade || '').toLowerCase()) {
      case 'grade a': return 'grade-a';
      case 'grade b': return 'grade-b';
      case 'grade c': return 'grade-c';
      default: return '';
    }
  }
}