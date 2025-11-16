// production.component.ts
import { Component, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
<<<<<<< HEAD
import { FormsModule } from '@angular/forms';
import { CooperativeSidebarComponent } from '../../../../shared/cooperative-sidebar/cooperative-sidebar.component';
=======
import { FormsModule, NgForm } from '@angular/forms';
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8

interface ProductionRecord {
  id: string;
  date: string;
  farmer: string;
  crop: string;
  quantity: string;
  grade: string;
  gradeClass: string;
  warehouse: string;
  value: string;
  status: string;
  statusClass: string;
}

interface NewProductionData {
  farmer: string;
  crop: string;
  quantity: number;
  grade: string;
  warehouse: string;
  date: string;
}

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, FormsModule],
<<<<<<< HEAD
  template: `<!-- HTML template content below -->`,
  styleUrls: ['./production.component.scss'],
  encapsulation: ViewEncapsulation.None
=======
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
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
})
export class ProductionComponent {
  user = { name: 'Emmanuel Njoya', role: 'Manager', initials: 'EN' };

  systemStatus = 'Online';
  activeMembers = 245;
  activeLoans = '68.5M XAF';
  stockAlertsCount = 18;

<<<<<<< HEAD
  totalProduction = '287.5 MT';
  totalProductionPercent = '+15.2% vs last cycle';
=======
  cropPrices: { [key: string]: number } = {
    Cocoa: 2100000,
    Coffee: 2200000,
    Maize: 500000,
    'Palm Oil': 1600000,
    Cotton: 1300000,
    Cassava: 500000,
  };
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8

  activeFarmers = 245;
  activeFarmersParticipation = '81.7% participation rate';

  gradeAProduction = '168.3 MT';
  gradeAPercent = '58.5% premium quality';

  thisMonthDeliveries = '42.8 MT';
  thisMonthChange = '+8.3% vs last month';

  searchQuery = '';
  selectedCrop = 'All Crops';
  selectedGrade = 'All Grades';

  cropTypes = ['All Crops', 'Cocoa', 'Coffee', 'Maize', 'Palm Oil', 'Cotton', 'Cassava'];
  qualityGrades = ['All Grades', 'Grade A', 'Grade B', 'Grade C'];

  // Modal states
  showRecordModal = false;
  showExportModal = false;

  // New production form data
  newProduction: NewProductionData = {
    farmer: '',
<<<<<<< HEAD
    crop: 'Cocoa',
    quantity: 0,
    grade: 'Grade A',
    warehouse: 'Douala Warehouse',
    date: new Date().toISOString().split('T')[0]
  };

  // Export options
  exportFormat = 'csv';
  exportDateRange = 'all';
  exportStartDate = '';
  exportEndDate = '';

  farmers = [
    'Kwame Osei (M001)',
    'Arna Boateng (M002)',
    'Yaw Mensah (M003)',
    'Akosua Darko (M004)',
    'Kofi Asante (M005)',
    'Abena Owusu (M006)',
    'Kwabena Amoah (M007)',
    'Efua Agyeman (M008)'
  ];

  warehouses = ['Douala Warehouse', 'Yaoundé Warehouse', 'Garoua Warehouse'];

  productionRecords: ProductionRecord[] = [
    { id: 'PROD001', date: '04/10/2024', farmer: 'Kwame Osei (M001)', crop: 'Cocoa', quantity: '2.5 MT', grade: 'Grade A', gradeClass: 'grade-a', warehouse: 'Douala Warehouse', value: '5,250,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD002', date: '04/10/2024', farmer: 'Arna Boateng (M002)', crop: 'Coffee', quantity: '1.8 MT', grade: 'Grade A', gradeClass: 'grade-a', warehouse: 'Yaoundé Warehouse', value: '3,960,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD003', date: '03/10/2024', farmer: 'Yaw Mensah (M003)', crop: 'Cocoa', quantity: '3.2 MT', grade: 'Grade B', gradeClass: 'grade-b', warehouse: 'Douala Warehouse', value: '6,080,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD004', date: '03/10/2024', farmer: 'Akosua Darko (M004)', crop: 'Maize', quantity: '4.5 MT', grade: 'Grade A', gradeClass: 'grade-a', warehouse: 'Garoua Warehouse', value: '2,250,000 XAF', status: 'Pending', statusClass: 'pending' },
    { id: 'PROD005', date: '02/10/2024', farmer: 'Kofi Asante (M005)', crop: 'Palm Oil', quantity: '1.2 MT', grade: 'Grade A', gradeClass: 'grade-a', warehouse: 'Douala Warehouse', value: '1,920,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD006', date: '02/10/2024', farmer: 'Abena Owusu (M006)', crop: 'Cotton', quantity: '5.8 MT', grade: 'Grade B', gradeClass: 'grade-b', warehouse: 'Garoua Warehouse', value: '7,540,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD007', date: '01/10/2024', farmer: 'Kwabena Amoah (M007)', crop: 'Cassava', quantity: '3.5 MT', grade: 'Grade A', gradeClass: 'grade-a', warehouse: 'Yaoundé Warehouse', value: '1,750,000 XAF', status: 'Verified', statusClass: 'verified' },
    { id: 'PROD008', date: '01/10/2024', farmer: 'Efua Agyeman (M008)', crop: 'Coffee', quantity: '2.1 MT', grade: 'Grade C', gradeClass: 'grade-c', warehouse: 'Yaoundé Warehouse', value: '3,780,000 XAF', status: 'Rejected', statusClass: 'rejected' },
  ];

  currentPage = 1;
  pageSize = 20;

  get filteredProduction(): ProductionRecord[] {
    return this.productionRecords.filter(record => {
      const searchLower = this.searchQuery.toLowerCase();
      const matchSearch =
        !this.searchQuery ||
        record.id.toLowerCase().includes(searchLower) ||
        record.farmer.toLowerCase().includes(searchLower) ||
        record.crop.toLowerCase().includes(searchLower);

      const matchCrop =
        this.selectedCrop === 'All Crops' || record.crop === this.selectedCrop;

      const matchGrade =
        this.selectedGrade === 'All Grades' || record.grade === this.selectedGrade;

      return matchSearch && matchCrop && matchGrade;
    });
  }

  get totalPages(): number {
    return Math.ceil(this.filteredProduction.length / this.pageSize) || 1;
=======
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
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
  }

  get paginatedProduction(): ProductionRecord[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredProduction.slice(startIndex, endIndex);
  }

<<<<<<< HEAD
  onExport(): void {
    this.showExportModal = true;
  }

  onRecordProduction(): void {
    this.showRecordModal = true;
  }

  closeRecordModal(): void {
    this.showRecordModal = false;
    this.resetNewProduction();
  }

  closeExportModal(): void {
    this.showExportModal = false;
  }

  resetNewProduction(): void {
    this.newProduction = {
      farmer: '',
      crop: 'Cocoa',
      quantity: 0,
      grade: 'Grade A',
      warehouse: 'Douala Warehouse',
      date: new Date().toISOString().split('T')[0]
    };
  }

  submitProduction(): void {
    if (!this.newProduction.farmer || this.newProduction.quantity <= 0) {
      alert('Please fill in all required fields');
      return;
=======
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
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
    }

    // Calculate value based on crop and grade
    const basePrice = this.getBasePrice(this.newProduction.crop);
    const gradeMultiplier = this.getGradeMultiplier(this.newProduction.grade);
    const totalValue = basePrice * this.newProduction.quantity * gradeMultiplier;

    // Generate new production ID
    const newId = `PROD${String(this.productionRecords.length + 1).padStart(3, '0')}`;

    // Format date
    const dateObj = new Date(this.newProduction.date);
    const formattedDate = `${String(dateObj.getDate()).padStart(2, '0')}/${String(dateObj.getMonth() + 1).padStart(2, '0')}/${dateObj.getFullYear()}`;

    // Create new record
    const newRecord: ProductionRecord = {
      id: newId,
      date: formattedDate,
      farmer: this.newProduction.farmer,
      crop: this.newProduction.crop,
      quantity: `${this.newProduction.quantity} MT`,
      grade: this.newProduction.grade,
      gradeClass: this.getGradeClass(this.newProduction.grade),
      warehouse: this.newProduction.warehouse,
      value: `${totalValue.toLocaleString()} XAF`,
      status: 'Pending',
      statusClass: 'pending'
    };

    // Add to beginning of array
    this.productionRecords.unshift(newRecord);

    // Show success message
    alert(`Production record ${newId} created successfully!`);

    // Close modal and reset form
    this.closeRecordModal();
  }

  performExport(): void {
    let dataToExport = this.filteredProduction;

    // Apply date range filter if needed
    if (this.exportDateRange === 'custom' && this.exportStartDate && this.exportEndDate) {
      dataToExport = dataToExport.filter(record => {
        const recordDate = this.parseDate(record.date);
        const startDate = new Date(this.exportStartDate);
        const endDate = new Date(this.exportEndDate);
        return recordDate >= startDate && recordDate <= endDate;
      });
    }

    if (dataToExport.length === 0) {
      alert('No data to export with current filters');
      return;
    }

    if (this.exportFormat === 'csv') {
      this.exportToCSV(dataToExport);
    } else if (this.exportFormat === 'json') {
      this.exportToJSON(dataToExport);
    } else if (this.exportFormat === 'pdf') {
      alert('PDF export would be implemented with a library like jsPDF');
    }

    this.closeExportModal();
  }

  exportToCSV(data: ProductionRecord[]): void {
    const headers = ['Production ID', 'Date', 'Farmer', 'Crop', 'Quantity', 'Grade', 'Warehouse', 'Value', 'Status'];
    const csvContent = [
      headers.join(','),
      ...data.map(record => [
        record.id,
        record.date,
        `"${record.farmer}"`,
        record.crop,
        record.quantity,
        record.grade,
        record.warehouse,
        record.value,
        record.status
      ].join(','))
    ].join('\n');

    this.downloadFile(csvContent, 'production-data.csv', 'text/csv');
  }

  exportToJSON(data: ProductionRecord[]): void {
    const jsonContent = JSON.stringify(data, null, 2);
    this.downloadFile(jsonContent, 'production-data.json', 'application/json');
  }

  downloadFile(content: string, filename: string, mimeType: string): void {
    const blob = new Blob([content], { type: mimeType });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  parseDate(dateStr: string): Date {
    const parts = dateStr.split('/');
    return new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]));
  }

  getBasePrice(crop: string): number {
    const prices: { [key: string]: number } = {
      'Cocoa': 2100000,
      'Coffee': 2200000,
      'Maize': 500000,
      'Palm Oil': 1600000,
      'Cotton': 1300000,
      'Cassava': 500000
    };
    return prices[crop] || 1000000;
  }

  getGradeMultiplier(grade: string): number {
    const multipliers: { [key: string]: number } = {
      'Grade A': 1.0,
      'Grade B': 0.95,
      'Grade C': 0.9
    };
    return multipliers[grade] || 1.0;
  }

<<<<<<< HEAD
  getGradeClass(grade: string): string {
    const classes: { [key: string]: string } = {
      'Grade A': 'grade-a',
      'Grade B': 'grade-b',
      'Grade C': 'grade-c'
    };
    return classes[grade] || 'grade-a';
  }

  onLanguageChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    alert('Language changed to ' + select.value);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
=======
  getGradeClass(grade: string | undefined): string {
    switch ((grade ?? '').toLowerCase()) {
      case 'grade a': return 'grade-a';
      case 'grade b': return 'grade-b';
      case 'grade c': return 'grade-c';
      default: return '';
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
    }
  }
}

export class ProductionComponent {
}
