import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PlantHealthService, DiseaseReport, PlantPhotoResponse, PlantHealthSummary, DiseaseReportSummary } from './plant-health.service';

interface HealthStat {
  title: string;
  value: string;
  change: string;
  icon: string;
  color: string;
  changeColor: string;
}

interface DiseaseReport {
  id: string;
  farmerName: string;
  crop: string;
  disease: string;
  location: string;
  reportDate: string;
  severity: string;
  status: string;
  affectedArea: string;
  hasImage: boolean;
}

@Component({
  selector: 'app-plant-health',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './plant-health.component.html',
  styleUrls: ['./plant-health.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class PlantHealthComponent implements OnInit {
  // User info from auth service
  user = {
    name: '',
    role: '',
    initials: '',
    cooperativeId: ''
  };

  // Filter states
  selectedSeverity: string = 'all';
  selectedCrop: string = 'all';
  searchQuery: string = '';
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;

  // Loading states
  isLoading: boolean = false;
  isLoadingStats: boolean = false;

  // Health stats
  healthStats: HealthStat[] = [];

  // Disease reports from backend
  diseaseReports: DiseaseReport[] = [];
  filteredDiseaseReports: DiseaseReport[] = [];

  // Modal states
  showReportModal: boolean = false;
  showViewReportModal: boolean = false;
  showTreatReportModal: boolean = false;
  selectedReport: DiseaseReport | null = null;

  // Report form data
  newReport: {
    farmerId: number | null;
    crop: string;
    disease: string;
    location: string;
    affectedArea: string;
    severity: string;
    reportDate: string;
    reportedBy: string;
    treatmentNotes: string;
    photos: File[];
  } = {
    farmerId: null,
    crop: '',
    disease: '',
    location: '',
    affectedArea: '',
    severity: 'MEDIUM',
    reportDate: new Date().toISOString().split('T')[0],
    reportedBy: '',
    treatmentNotes: '',
    photos: []
  };

  // Available options
  severityOptions = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'CRITICAL', label: 'Critical' }
  ];

  cropOptions = [
    'Cocoa', 'Coffee', 'Cotton', 'Palm Oil', 'Cassava', 'Maize', 'Rice', 'Banana', 'Pineapple'
  ];

  diseaseOptions = [
    'Black Pod Disease', 'Coffee Berry Disease', 'Fusarium Wilt', 'Bacterial Blight',
    'Leaf Spot', 'Root Rot', 'Powdery Mildew', 'Downy Mildew', 'Anthracnose',
    'Cercospora Leaf Spot', 'Rust Disease', 'Blight', 'Wilt', 'Other'
  ];

  constructor(
    private authService: AuthService,
    private plantHealthService: PlantHealthService
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadHealthStats();
    this.loadDiseaseReports();
  }

  private loadUserInfo(): void {
    // Get user info from auth service
    this.authService.getCurrentUser().subscribe(user => {
      if (user) {
        this.user = {
          name: user.name || '',
          role: user.role || '',
          initials: this.getInitials(user.name || ''),
          cooperativeId: user.cooperativeId || ''
        };
        this.newReport.reportedBy = user.name || '';
        this.newReport.farmerId = user.id || null;
      }
    });
  }

  private loadHealthStats(): void {
    this.isLoadingStats = true;

    // Load disease report summary
    this.plantHealthService.getDiseaseReportSummary().subscribe(summary => {
      this.healthStats = [
        {
          title: 'Active Cases',
          value: summary.activeCases.toString(),
          change: `${summary.criticalCases} critical`,
          icon: '⚠️',
          color: 'bg-warning',
          changeColor: 'text-warning'
        },
        {
          title: 'Resolved Cases',
          value: summary.resolvedCases.toString(),
          change: '+18 this month',
          icon: '✅',
          color: 'bg-success',
          changeColor: 'text-success'
        },
        {
          title: 'Total Reports',
          value: summary.totalReports.toString(),
          change: 'All time',
          icon: '📊',
          color: 'bg-info',
          changeColor: 'text-info'
        },
        {
          title: 'Critical Alerts',
          value: summary.criticalCases.toString(),
          change: 'Immediate attention',
          icon: '🔴',
          color: 'bg-destructive',
          changeColor: 'text-destructive'
        }
      ];
      this.isLoadingStats = false;
    });
  }

  private loadDiseaseReports(): void {
    this.isLoading = true;

    this.plantHealthService.getDiseaseReportsPaginated({
      page: this.currentPage,
      size: this.pageSize
    }).subscribe(response => {
      this.diseaseReports = response.content || [];
      this.filteredDiseaseReports = [...this.diseaseReports];
      this.totalPages = response.totalPages || 0;
      this.isLoading = false;
    });
  }

  onFilterChange(): void {
    this.filteredDiseaseReports = this.diseaseReports.filter(report => {
      const matchesSeverity = this.selectedSeverity === 'all' || report.severity === this.selectedSeverity;
      const matchesCrop = this.selectedCrop === 'all' || report.crop === this.selectedCrop;
      const matchesSearch = !this.searchQuery ||
        report.disease.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        report.crop.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        report.location.toLowerCase().includes(this.searchQuery.toLowerCase());

      return matchesSeverity && matchesCrop && matchesSearch;
    });
  }

  // Modal methods
  openReportModal(): void {
    this.showReportModal = true;
  }

  closeReportModal(): void {
    this.showReportModal = false;
    this.resetReportForm();
  }

  openViewReportModal(report: DiseaseReport): void {
    this.selectedReport = report;
    this.showViewReportModal = true;
  }

  closeViewReportModal(): void {
    this.showViewReportModal = false;
    this.selectedReport = null;
  }

  openTreatReportModal(report: DiseaseReport): void {
    this.selectedReport = report;
    this.showTreatReportModal = true;
  }

  closeTreatReportModal(): void {
    this.showTreatReportModal = false;
    this.selectedReport = null;
  }

  private resetReportForm(): void {
    this.newReport = {
      farmerId: this.user.cooperativeId ? parseInt(this.user.cooperativeId) : null,
      crop: '',
      disease: '',
      location: '',
      affectedArea: '',
      severity: 'MEDIUM',
      reportDate: new Date().toISOString().split('T')[0],
      reportedBy: this.user.name,
      treatmentNotes: '',
      photos: []
    };
  }

  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.newReport.photos = Array.from(files);
    }
  }

  takePhoto(): void {
    // Implement camera functionality
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      navigator.mediaDevices.getUserMedia({ video: true })
        .then(stream => {
          // Camera access granted - implement photo capture
          console.log('Camera access granted');
        })
        .catch(error => {
          console.error('Camera access denied:', error);
        });
    }
  }

  getCurrentLocation(): void {
    this.plantHealthService.getCurrentLocation()
      .then(location => {
        this.newReport.location = `${location.latitude.toFixed(6)}, ${location.longitude.toFixed(6)}`;
      })
      .catch(error => {
        console.error('Error getting location:', error);
        // Fallback to manual entry
      });
  }

  submitDiseaseReport(): void {
    if (!this.isReportFormValid()) {
      return;
    }

    const request = {
      farmerId: this.newReport.farmerId!,
      crop: this.newReport.crop,
      disease: this.newReport.disease,
      location: this.newReport.location,
      affectedArea: this.newReport.affectedArea,
      severity: this.newReport.severity as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
      reportDate: new Date(this.newReport.reportDate),
      reportedBy: this.newReport.reportedBy,
      treatmentNotes: this.newReport.treatmentNotes,
      photos: this.newReport.photos
    };

    this.plantHealthService.createQuickDiseaseReport(request).subscribe(reports => {
      if (reports && reports.length > 0) {
        this.loadDiseaseReports(); // Refresh the list
        this.closeReportModal();
        // Show success message
      }
    });
  }

  updateReportStatus(report: DiseaseReport, status: string, treatmentNotes?: string): void {
    this.plantHealthService.updateReportStatus(report.id!, status, treatmentNotes).subscribe(updatedReport => {
      this.loadDiseaseReports(); // Refresh the list
      this.closeTreatReportModal();
    });
  }

  private isReportFormValid(): boolean {
    return !!(
      this.newReport.farmerId &&
      this.newReport.crop &&
      this.newReport.disease &&
      this.newReport.location &&
      this.newReport.affectedArea &&
      this.newReport.severity &&
      this.newReport.reportedBy
    );
  }

  // Pagination methods
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadDiseaseReports();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadDiseaseReports();
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadDiseaseReports();
    }
  }

  // Utility methods
  private getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getSeverityClass(severity: string): string {
    switch (severity) {
      case 'CRITICAL': return 'severity-critical';
      case 'HIGH': return 'severity-high';
      case 'MEDIUM': return 'severity-medium';
      case 'LOW': return 'severity-low';
      default: return 'severity-medium';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'RESOLVED': return 'status-resolved';
      case 'TREATED': return 'status-treated';
      case 'CONFIRMED': return 'status-confirmed';
      case 'UNDER_REVIEW': return 'status-under-review';
      case 'PENDING': return 'status-pending';
      default: return 'status-pending';
    }
  }

  formatDate(date: Date | string): string {
    const d = new Date(date);
    return d.toLocaleDateString();
  }
}
      farmerName: 'Christine Ngo',
      crop: 'Cassava',
      disease: 'Cassava Mosaic Virus',
      location: 'Douala Zone 1',
      reportDate: '2025-10-02',
      severity: 'High',
      status: 'Resolved',
      affectedArea: '3.0 hectares',
      hasImage: true
    },
    {
      id: 'DIS-005',
      farmerName: 'André Tchoua',
      crop: 'Maize',
      disease: 'Maize Streak Virus',
      location: 'Yaoundé Zone 2',
      reportDate: '2025-10-01',
      severity: 'Low',
      status: 'Monitoring',
      affectedArea: '0.5 hectares',
      hasImage: false
    },
    {
      id: 'DIS-006',
      farmerName: 'Elisabeth Fon',
      crop: 'Cotton',
      disease: 'Cotton Bollworm',
      location: 'Garoua Zone 1',
      reportDate: '2025-09-30',
      severity: 'Medium',
      status: 'Under Treatment',
      affectedArea: '1.5 hectares',
      hasImage: true
    }
  ];

  // Filtered reports
  filteredReports: DiseaseReport[] = [];

  // Modal states
  showReportModal = false;
  showViewModal = false;
  showTreatModal = false;
  showEditModal = false;
  selectedReport: DiseaseReport | null = null;

  // Form data for reporting
  reportForm = {
    farmerName: '',
    crop: '',
    disease: '',
    location: '',
    affectedArea: '',
    severity: 'Medium',
    description: '',
    image: null as File | null
  };

  // Form data for treatment
  treatmentForm = {
    treatmentType: '',
    treatmentDate: '',
    treatmentNotes: '',
    followUpDate: '',
    assignedExpert: ''
  };

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 1;

  // Crop options
  crops = [
    { value: 'all', label: 'All Crops' },
    { value: 'cocoa', label: 'Cocoa' },
    { value: 'coffee', label: 'Coffee' },
    { value: 'palmoil', label: 'Palm Oil' },
    { value: 'cotton', label: 'Cotton' },
    { value: 'cassava', label: 'Cassava' },
    { value: 'maize', label: 'Maize' }
  ];

  // Severity options
  severities = [
    { value: 'all', label: 'All Severities' },
    { value: 'critical', label: 'Critical' },
    { value: 'high', label: 'High' },
    { value: 'medium', label: 'Medium' },
    { value: 'low', label: 'Low' }
  ];

  constructor(private authService: AuthService) {}


  get allFilteredReports(): DiseaseReport[] {
    let filtered = [...this.diseaseReports];

    // Filter by search query
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(report =>
        report.farmerName.toLowerCase().includes(query) ||
        report.disease.toLowerCase().includes(query) ||
        report.location.toLowerCase().includes(query) ||
        report.crop.toLowerCase().includes(query)
      );
    }

    // Filter by crop
    if (this.selectedCrop !== 'all') {
      filtered = filtered.filter(report =>
        report.crop.toLowerCase() === this.selectedCrop.toLowerCase()
      );
    }

    // Filter by severity
    if (this.selectedSeverity !== 'all') {
      filtered = filtered.filter(report =>
        report.severity.toLowerCase() === this.selectedSeverity.toLowerCase()
      );
    }

    return filtered;
  }
  
  get paginatedReports(): DiseaseReport[] {
    const filtered = this.allFilteredReports;
    this.totalPages = Math.ceil(filtered.length / this.itemsPerPage);
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return filtered.slice(startIndex, endIndex);
  }

  applyFilters(): void {
    this.currentPage = 1;
  }
  
  get Math() {
    return Math;
  }

  onSearchChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onCropChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }

  onSeverityChange(): void {
    this.currentPage = 1;
    this.applyFilters();
  }
  
  // Pagination methods
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);
    
    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  // Helper methods for template
  getPaginationStart(): number {
    return (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  getPaginationEnd(): number {
    return Math.min(this.currentPage * this.itemsPerPage, this.allFilteredReports.length);
  }

  getTotalItems(): number {
    return this.allFilteredReports.length;
  }

  getSeverityColor(severity: string): string {
    switch (severity.toLowerCase()) {
      case 'critical':
        return 'badge-critical';
      case 'high':
        return 'badge-high';
      case 'medium':
        return 'badge-medium';
      case 'low':
        return 'badge-low';
      default:
        return 'badge-default';
    }
  }

  getStatusColor(status: string): string {
    switch (status.toLowerCase()) {
      case 'resolved':
        return 'status-resolved';
      case 'under treatment':
        return 'status-treatment';
      case 'investigating':
        return 'status-investigating';
      case 'monitoring':
        return 'status-monitoring';
      default:
        return 'status-default';
    }
  }

  reportDisease(): void {
    this.resetReportForm();
    this.showReportModal = true;
  }

  takePhoto(): void {
    // Implement photo capture functionality
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.capture = 'environment';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.reportForm.image = file;
        console.log('Photo captured:', file.name);
      }
    };
    input.click();
  }

  uploadImage(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (event: any) => {
      const file = event.target.files[0];
      if (file) {
        this.reportForm.image = file;
        console.log('Image uploaded:', file.name);
      }
    };
    input.click();
  }

  setLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const coords = `${position.coords.latitude.toFixed(6)}, ${position.coords.longitude.toFixed(6)}`;
          this.reportForm.location = coords;
          console.log('Location set:', coords);
        },
        (error) => {
          console.error('Error getting location:', error);
          alert('Unable to get location. Please enter manually.');
        }
      );
    } else {
      alert('Geolocation is not supported by this browser.');
    }
  }

  viewReport(report: DiseaseReport): void {
    this.selectedReport = report;
    this.showViewModal = true;
  }

  editReport(report: DiseaseReport): void {
    this.selectedReport = report;
    this.populateEditForm(report);
    this.showEditModal = true;
  }

  treatReport(report: DiseaseReport): void {
    this.selectedReport = report;
    this.resetTreatmentForm();
    this.showTreatModal = true;
  }

  deleteReport(report: DiseaseReport): void {
    if (confirm(`Are you sure you want to delete the disease report for ${report.farmerName}?`)) {
      const index = this.diseaseReports.findIndex(r => r.id === report.id);
      if (index > -1) {
        this.diseaseReports.splice(index, 1);
        this.applyFilters();
        alert('Disease report deleted successfully!');
      }
    }
  }

  // Modal management methods
  closeReportModal(): void {
    this.showReportModal = false;
    this.resetReportForm();
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedReport = null;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedReport = null;
    this.resetReportForm();
  }

  closeTreatModal(): void {
    this.showTreatModal = false;
    this.selectedReport = null;
    this.resetTreatmentForm();
  }

  // Form management methods
  resetReportForm(): void {
    this.reportForm = {
      farmerName: '',
      crop: '',
      disease: '',
      location: '',
      affectedArea: '',
      severity: 'Medium',
      description: '',
      image: null
    };
  }

  populateEditForm(report: DiseaseReport): void {
    this.reportForm = {
      farmerName: report.farmerName,
      crop: report.crop,
      disease: report.disease,
      location: report.location,
      affectedArea: report.affectedArea,
      severity: report.severity,
      description: '', // Description might not be stored in the report object
      image: null
    };
  }

  resetTreatmentForm(): void {
    this.treatmentForm = {
      treatmentType: '',
      treatmentDate: new Date().toISOString().split('T')[0],
      treatmentNotes: '',
      followUpDate: '',
      assignedExpert: ''
    };
  }

  // Form submission methods
  submitDiseaseReport(): void {
    if (!this.validateReportForm()) {
      return;
    }

    console.log('Submitting disease report:', this.reportForm);
    // Add new report to the list
    const newReport: DiseaseReport = {
      id: `DIS-${String(this.diseaseReports.length + 1).padStart(3, '0')}`,
      farmerName: this.reportForm.farmerName,
      crop: this.reportForm.crop,
      disease: this.reportForm.disease,
      location: this.reportForm.location,
      reportDate: new Date().toISOString().split('T')[0],
      severity: this.reportForm.severity,
      status: 'Reported',
      affectedArea: this.reportForm.affectedArea,
      hasImage: this.reportForm.image !== null
    };

    this.diseaseReports.unshift(newReport);
    this.applyFilters();
    this.closeReportModal();

    alert('Disease report submitted successfully!');
  }

  updateDiseaseReport(): void {
    if (!this.validateReportForm() || !this.selectedReport) {
      return;
    }

    console.log('Updating disease report:', this.selectedReport.id, this.reportForm);

    // Find and update the report
    const index = this.diseaseReports.findIndex(r => r.id === this.selectedReport!.id);
    if (index > -1) {
      this.diseaseReports[index] = {
        ...this.diseaseReports[index],
        farmerName: this.reportForm.farmerName,
        crop: this.reportForm.crop,
        disease: this.reportForm.disease,
        location: this.reportForm.location,
        severity: this.reportForm.severity,
        affectedArea: this.reportForm.affectedArea,
        hasImage: this.reportForm.image !== null
      };

      this.applyFilters();
      this.closeEditModal();

      alert('Disease report updated successfully!');
    }
  }

  submitTreatment(): void {
    if (!this.validateTreatmentForm()) {
      return;
    }

    console.log('Submitting treatment for report:', this.selectedReport?.id, this.treatmentForm);
    if (this.selectedReport) {
      // Update report status to "Under Treatment"
      this.selectedReport.status = 'Under Treatment';

      // Add treatment record (in a real app, this would be saved to backend)
      console.log('Treatment recorded:', {
        reportId: this.selectedReport.id,
        ...this.treatmentForm
      });
    }

    this.closeTreatModal();
    alert('Treatment plan submitted successfully!');
  }

  // Validation methods
  validateReportForm(): boolean {
    if (!this.reportForm.farmerName || !this.reportForm.crop || !this.reportForm.disease ||
        !this.reportForm.location || !this.reportForm.affectedArea) {
      alert('Please fill in all required fields.');
      return false;
    }
    return true;
  }

  validateTreatmentForm(): boolean {
    if (!this.treatmentForm.treatmentType || !this.treatmentForm.treatmentDate) {
      alert('Please fill in all required fields.');
      return false;
    }
    return true;
  }
}
