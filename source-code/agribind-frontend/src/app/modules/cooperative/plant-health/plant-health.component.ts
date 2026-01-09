import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PlantHealthService, DiseaseReport, PlantHealthSummary, DiseaseReportSummary } from './plant-health.service';
import { Observable, of } from 'rxjs';

interface HealthStat {
  title: string;
  value: string;
  change: string;
  icon: string;
  color: string;
  changeColor: string;
}

interface NewReportForm {
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
}

interface TreatmentForm {
  treatmentType: string;
  treatmentDate: string;
  treatmentNotes: string;
  followUpDate: string;
  assignedExpert: string;
}

interface UserInfo {
  id?: number;
  name?: string;
  role?: string;
  cooperativeId?: string;
  email?: string;
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
  showViewModal: boolean = false;
  showTreatModal: boolean = false;
  showEditModal: boolean = false;
  selectedReport: DiseaseReport | null = null;

  // Report form data
  newReport: NewReportForm = {
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

  // Form data for editing
  reportForm = {
    farmerName: '',
    crop: '',
    disease: '',
    location: '',
    affectedArea: '',
    severity: '',
    image: null as File | null
  };

  // Form data for treatment
  treatmentForm: TreatmentForm = {
    treatmentType: '',
    treatmentDate: new Date().toISOString().split('T')[0],
    treatmentNotes: '',
    followUpDate: '',
    assignedExpert: ''
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

  // Crop and severity filters
  crops = [
    { value: 'all', label: 'All Crops' },
    { value: 'cocoa', label: 'Cocoa' },
    { value: 'coffee', label: 'Coffee' },
    { value: 'palmoil', label: 'Palm Oil' },
    { value: 'cotton', label: 'Cotton' },
    { value: 'cassava', label: 'Cassava' },
    { value: 'maize', label: 'Maize' }
  ];

  severities = [
    { value: 'all', label: 'All Severities' },
    { value: 'CRITICAL', label: 'Critical' },
    { value: 'HIGH', label: 'High' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'LOW', label: 'Low' }
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
    try {
      // Get user synchronously (assuming getCurrentUser returns UserInfo directly)
      const user = this.authService.getCurrentUser();
      
      // Handle null/undefined case
      if (!user) {
        console.warn('No user information available');
        this.setDefaultUserInfo();
        return;
      }
      
      // Type assertion for safety
      const userInfo = user as any;
      
      // Set user information with null checks
      this.user = {
        name: userInfo.name || '',
        role: userInfo.role || '',
        initials: this.getInitials(userInfo.name || ''),
        cooperativeId: userInfo.cooperativeId || ''
      };
      
      // Set form defaults
      this.newReport.reportedBy = userInfo.name || '';
      this.newReport.farmerId = userInfo.id || null;
      
      // If cooperativeId is available and farmerId is still null, use it
      if (!this.newReport.farmerId && userInfo.cooperativeId) {
        const coopIdNum = parseInt(userInfo.cooperativeId, 10);
        if (!isNaN(coopIdNum)) {
          this.newReport.farmerId = coopIdNum;
        }
      }
    } catch (error) {
      console.error('Error loading user information:', error);
      this.setDefaultUserInfo();
    }
  }

  private setDefaultUserInfo(): void {
    this.user = {
      name: 'Guest User',
      role: 'Farmer',
      initials: 'GU',
      cooperativeId: ''
    };
    this.newReport.reportedBy = 'Guest User';
    this.newReport.farmerId = null;
  }

  private loadHealthStats(): void {
    this.isLoadingStats = true;

    this.plantHealthService.getDiseaseReportSummary().subscribe({
      next: (summary: DiseaseReportSummary) => {
        this.healthStats = [
          {
            title: 'Active Cases',
            value: summary.activeCases?.toString() || '0',
            change: `${summary.criticalCases || 0} critical`,
            icon: '⚠️',
            color: 'bg-warning',
            changeColor: 'text-warning'
          },
          {
            title: 'Resolved Cases',
            value: summary.resolvedCases?.toString() || '0',
            change: '+18 this month',
            icon: '✅',
            color: 'bg-success',
            changeColor: 'text-success'
          },
          {
            title: 'Total Reports',
            value: summary.totalReports?.toString() || '0',
            change: 'All time',
            icon: '📊',
            color: 'bg-info',
            changeColor: 'text-info'
          },
          {
            title: 'Critical Alerts',
            value: summary.criticalCases?.toString() || '0',
            change: 'Immediate attention',
            icon: '🔴',
            color: 'bg-destructive',
            changeColor: 'text-destructive'
          }
        ];
        this.isLoadingStats = false;
      },
      error: (error) => {
        console.error('Error loading health stats:', error);
        this.setDefaultHealthStats();
        this.isLoadingStats = false;
      }
    });
  }

  private setDefaultHealthStats(): void {
    this.healthStats = [
      {
        title: 'Active Cases',
        value: '0',
        change: '0 critical',
        icon: '⚠️',
        color: 'bg-warning',
        changeColor: 'text-warning'
      },
      {
        title: 'Resolved Cases',
        value: '0',
        change: '+0 this month',
        icon: '✅',
        color: 'bg-success',
        changeColor: 'text-success'
      },
      {
        title: 'Total Reports',
        value: '0',
        change: 'All time',
        icon: '📊',
        color: 'bg-info',
        changeColor: 'text-info'
      },
      {
        title: 'Critical Alerts',
        value: '0',
        change: 'No critical alerts',
        icon: '🔴',
        color: 'bg-destructive',
        changeColor: 'text-destructive'
      }
    ];
  }

  private loadDiseaseReports(): void {
    this.isLoading = true;

    this.plantHealthService.getDiseaseReportsPaginated({
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        this.diseaseReports = response.content || [];
        this.filteredDiseaseReports = [...this.diseaseReports];
        this.totalPages = response.totalPages || 0;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading disease reports:', error);
        this.diseaseReports = [];
        this.filteredDiseaseReports = [];
        this.totalPages = 0;
        this.isLoading = false;
      }
    });
  }

  onFilterChange(): void {
    this.filteredDiseaseReports = this.diseaseReports.filter(report => {
      const matchesSeverity = this.selectedSeverity === 'all' || report.severity === this.selectedSeverity;
      const matchesCrop = this.selectedCrop === 'all' || report.crop?.toLowerCase() === this.selectedCrop.toLowerCase();
      const matchesSearch = !this.searchQuery ||
        (report.disease?.toLowerCase().includes(this.searchQuery.toLowerCase()) ?? false) ||
        (report.crop?.toLowerCase().includes(this.searchQuery.toLowerCase()) ?? false) ||
        (report.location?.toLowerCase().includes(this.searchQuery.toLowerCase()) ?? false) ||
        (report.reportedBy?.toLowerCase().includes(this.searchQuery.toLowerCase()) ?? false);

      return matchesSeverity && matchesCrop && matchesSearch;
    });
  }

  onSearchChange(): void {
    this.onFilterChange();
  }

  onCropChange(): void {
    this.onFilterChange();
  }

  onSeverityChange(): void {
    this.onFilterChange();
  }

  // Modal methods
  openReportModal(): void {
    // Ensure user info is loaded
    if (!this.newReport.reportedBy) {
      this.loadUserInfo();
    }
    this.showReportModal = true;
  }

  closeReportModal(): void {
    this.showReportModal = false;
    this.resetReportForm();
  }

  openViewReportModal(report: DiseaseReport): void {
    this.selectedReport = report;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedReport = null;
  }

  openTreatReportModal(report: DiseaseReport): void {
    this.selectedReport = report;
    this.treatmentForm.treatmentDate = new Date().toISOString().split('T')[0];
    this.showTreatModal = true;
  }

  closeTreatModal(): void {
    this.showTreatModal = false;
    this.selectedReport = null;
    this.resetTreatmentForm();
  }

  openEditModal(report: DiseaseReport): void {
    this.selectedReport = report;
    // Populate form with report data
    this.reportForm = {
      farmerName: report.reportedBy || '',
      crop: report.crop || '',
      disease: report.disease || '',
      location: report.location || '',
      affectedArea: report.affectedArea || '',
      severity: report.severity || 'MEDIUM',
      image: null
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
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

  private resetTreatmentForm(): void {
    this.treatmentForm = {
      treatmentType: '',
      treatmentDate: new Date().toISOString().split('T')[0],
      treatmentNotes: '',
      followUpDate: '',
      assignedExpert: ''
    };
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.newReport.photos = Array.from(input.files);
    }
  }

  takePhoto(): void {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      navigator.mediaDevices.getUserMedia({ video: true })
        .then(stream => {
          console.log('Camera access granted');
          // Here you would typically handle the camera stream
          // For now, just show a success message
          alert('Camera access granted. Photo functionality would be implemented here.');
        })
        .catch(error => {
          console.error('Camera access denied:', error);
          alert('Unable to access camera. Please check permissions.');
        });
    } else {
      alert('Camera not available on this device.');
    }
  }

  uploadImage(): void {
    const input = document.getElementById('photoInput') as HTMLInputElement;
    if (input) {
      input.click();
    }
  }

  setLocation(): void {
    if (navigator.geolocation) {
      this.plantHealthService.getCurrentLocation()
        .then(location => {
          this.newReport.location = `${location.latitude.toFixed(6)}, ${location.longitude.toFixed(6)}`;
        })
        .catch(error => {
          console.error('Error getting location:', error);
          alert('Unable to get current location. Please enter manually.');
        });
    } else {
      alert('Geolocation is not supported by this browser.');
    }
  }

  reportDisease(): void {
    this.openReportModal();
  }

  submitDiseaseReport(): void {
    if (!this.isReportFormValidPublic()) {
      alert('Please fill in all required fields marked with *');
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

    this.plantHealthService.createQuickDiseaseReport(request).subscribe({
      next: (reports) => {
        if (reports && reports.length > 0) {
          alert('Disease report submitted successfully!');
          this.loadDiseaseReports();
          this.closeReportModal();
        }
      },
      error: (error) => {
        console.error('Error submitting disease report:', error);
        alert('Failed to submit disease report. Please try again.');
      }
    });
  }

  updateDiseaseReport(): void {
    if (!this.selectedReport) {
      return;
    }
    
    // Validate form
    if (!this.reportForm.crop || !this.reportForm.disease || !this.reportForm.location || !this.reportForm.affectedArea) {
      alert('Please fill in all required fields');
      return;
    }

    // Update logic would go here
    console.log('Updating report:', this.selectedReport.id, this.reportForm);
    this.closeEditModal();
  }

  submitTreatment(): void {
    if (!this.selectedReport) {
      return;
    }

    if (!this.treatmentForm.treatmentType || !this.treatmentForm.treatmentDate) {
      alert('Please fill in treatment type and date');
      return;
    }

    const status = 'UNDER_REVIEW';
    this.plantHealthService.updateReportStatus(
      this.selectedReport.id!,
      status,
      this.treatmentForm.treatmentNotes
    ).subscribe({
      next: (updatedReport) => {
        alert('Treatment plan submitted successfully!');
        this.loadDiseaseReports();
        this.closeTreatModal();
      },
      error: (error) => {
        console.error('Error submitting treatment:', error);
        alert('Failed to submit treatment plan. Please try again.');
      }
    });
  }

  treatReport(report: DiseaseReport): void {
    this.openTreatReportModal(report);
  }

  updateReportStatus(report: DiseaseReport, status: string, treatmentNotes?: string): void {
    this.plantHealthService.updateReportStatus(report.id!, status, treatmentNotes).subscribe({
      next: (updatedReport) => {
        this.loadDiseaseReports();
        this.closeTreatModal();
      },
      error: (error) => {
        console.error('Error updating report status:', error);
        alert('Failed to update report status.');
      }
    });
  }

  // Public validation method for template
  isReportFormValidPublic(): boolean {
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
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page - 1;
      this.loadDiseaseReports();
    }
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    const currentPageDisplay = this.currentPage + 1;
    let start = Math.max(1, currentPageDisplay - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages, start + maxVisible - 1);
    
    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1);
    }
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  getPaginationStart(): number {
    return this.currentPage * this.pageSize + 1;
  }

  getPaginationEnd(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.filteredDiseaseReports.length);
  }

  getTotalItems(): number {
    return this.filteredDiseaseReports.length;
  }

  // Utility methods
  private getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getSeverityClass(severity: string): string {
    switch (severity?.toUpperCase()) {
      case 'CRITICAL': return 'badge-critical';
      case 'HIGH': return 'badge-high';
      case 'MEDIUM': return 'badge-medium';
      case 'LOW': return 'badge-low';
      default: return 'badge-medium';
    }
  }

  getSeverityColor(severity: string): string {
    return this.getSeverityClass(severity);
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'RESOLVED': return 'status-resolved';
      case 'TREATED': return 'status-treatment';
      case 'CONFIRMED': return 'status-confirmed';
      case 'UNDER_REVIEW': return 'status-under-review';
      case 'PENDING': return 'status-pending';
      default: return 'status-pending';
    }
  }

  getStatusColor(status: string): string {
    return this.getStatusClass(status);
  }

  formatDate(date: Date | string | null | undefined): string {
    if (!date) return 'N/A';
    try {
      const d = new Date(date);
      return isNaN(d.getTime()) ? 'Invalid Date' : d.toLocaleDateString();
    } catch (error) {
      return 'Invalid Date';
    }
  }

  // Helper method to check if user is logged in
  isUserLoggedIn(): boolean {
    return !!this.user.name && this.user.name !== 'Guest User';
  }
}