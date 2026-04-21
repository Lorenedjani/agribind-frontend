// src/app/modules/cooperative/members/member-list/member-list.component.ts
// UPDATED VERSION with QR Code and Export functionality

import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

// Services
import { UserService, CreateUserCommand } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { QrCodeService, QRCodeResponse } from '../../../../core/services/qr-code.service';
import { ExportService } from '../../../../core/services/export.service';
import { HttpClient } from '@angular/common/http';

// Components
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';

interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  gpsCoordinates?: string;
  primaryCrop: string;
  status: string;
  email?: string;
  registrationNumber?: string;
}

@Component({
  selector: 'app-member-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MemberFormComponent,
    EditMemberFormComponent,
    MemberDetailComponent,
    DeleteMemberComponent
  ],
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss']
})
export class MemberListComponent implements OnInit, OnDestroy {
  @ViewChild(MemberFormComponent) memberFormComponent!: MemberFormComponent;
  @ViewChild(EditMemberFormComponent) editMemberFormComponent!: EditMemberFormComponent;
  @ViewChild(MemberDetailComponent) memberDetailComponent!: MemberDetailComponent;
  @ViewChild(DeleteMemberComponent) deleteMemberComponent!: DeleteMemberComponent;

  private destroy$ = new Subject<void>();

  members: Member[] = [];
  filteredMembers: Member[] = [];
  paginatedMembers: Member[] = [];
  isLoading = false;
  errorMessage = '';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Search and Filters
  searchQuery = '';
  selectedStatus = 'All';
  selectedRegion = 'All';
  selectedCrop = 'All';

  // Filter options
  statusOptions = ['All', 'ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED'];
  regionOptions = ['All', 'ADAMAOUA', 'CENTRE', 'EST', 'EXTREME_NORD', 'LITTORAL',
                   'NORD', 'NORD_OUEST', 'OUEST', 'SUD', 'SUD_OUEST'];
  cropOptions = ['All', 'COCOA', 'COFFEE', 'CASSAVA', 'MAIZE', 'RICE', 'COTTON', 'PALM_OIL'];

  // Tab System
  activeTab: 'farmers' | 'managers' = 'farmers';
  isCooperativeAdmin = false;
  managersCount = 0;

  // User info
  user: any = {
    name: 'Cooperative Manager',
    role: 'Cooperative',
    initials: 'CM',
    email: ''
  };

  // QR Code modal
  showQRModal = false;
  selectedQRCode: QRCodeResponse | null = null;
  qrCodeLoading = false;

  // Export
  exportLoading = false;

  // Create Manager Modal
  showManagerModal = false;
  isSubmittingManager = false;
  managerForm = { name: '', phone: '', email: '', region: '' };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private qrCodeService: QrCodeService,
    private exportService: ExportService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadUserInfo();
    const currentUser = this.authService.getCurrentUser();
    // Admin = COOPERATIVE role; Manager = COOPERATIVE_MANAGER role
    this.isCooperativeAdmin = currentUser?.role === 'COOPERATIVE';
    this.loadMembers();
    if (this.isCooperativeAdmin) { this.loadManagersCount(); }
  }

  // Switch between Farmers and Managers tabs
  switchTab(tab: 'farmers' | 'managers'): void {
    this.activeTab = tab;
    this.currentPage = 1;
    this.searchQuery = '';
    this.selectedStatus = 'All';
    this.selectedRegion = 'All';
    this.loadMembers();
  }

  loadManagersCount(): void {
    this.userService.getUsers(0, 1, { type: 'COOPERATIVE_MANAGER' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (r: any) => { this.managersCount = r.totalElements ?? 0; }, error: () => {} });
  }

  // ── Manager Modal ────────────────────────────────────────────────────
  openAddManagerModal(): void {
    this.managerForm = { name: '', phone: '+237 ', email: '', region: '' };
    this.showManagerModal = true;
  }

  closeManagerModal(): void {
    this.showManagerModal = false;
    this.isSubmittingManager = false;
  }

  submitCreateManager(): void {
    if (!this.managerForm.name || !this.managerForm.phone || !this.managerForm.region) {
      alert('Please fill in all required fields (Name, Phone, Region).');
      return;
    }
    this.isSubmittingManager = true;
    const coopScope = this.cooperativeUserIdForScope();
    if (!coopScope) {
      alert('Impossible de déterminer la coopérative. Reconnectez-vous.');
      this.isSubmittingManager = false;
      return;
    }
    const cmd: CreateUserCommand = {
      type: 'COOPERATIVE_MANAGER' as any,
      name: this.managerForm.name.trim(),
      email: this.managerForm.email?.trim() || undefined,
      phoneNumber: this.managerForm.phone.replace(/\s/g, ''),
      region: this.managerForm.region as any,
      preferredLanguage: 'fr',
      cooperativeUserId: coopScope
    };
    this.userService.createUser(cmd)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: any) => {
          alert(`✅ Manager "${user.name}" created successfully! Credentials sent.`);
          this.closeManagerModal();
          this.loadManagersCount();
          if (this.activeTab === 'managers') { this.loadMembers(); }
          this.isSubmittingManager = false;
        },
        error: (err: any) => {
          alert('❌ ' + this.formatErrorMessage(err));
          this.isSubmittingManager = false;
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** Cooperative scope for creating farmers/managers (JWT / login user). */
  private cooperativeUserIdForScope(): string | undefined {
    const cu = this.authService.getCurrentUser();
    if (!cu) return undefined;
    if (cu.cooperativeId) return cu.cooperativeId;
    const role = (cu.role || '').toUpperCase();
    if (role === 'COOPERATIVE' || role === 'COOPERATIVE_MANAGER') {
      return cu.userId;
    }
    return undefined;
  }

  loadUserInfo() {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.user = {
        name: currentUser.username || currentUser.email || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || currentUser.email || 'User'),
        email: currentUser.email || ''
      };
    }
  }

  loadMembers() {
    this.isLoading = true;
    this.errorMessage = '';

    const filters: any = {};

    // Filter by type from API based on active tab
    if (this.activeTab === 'managers') {
      filters.type = 'COOPERATIVE_MANAGER';
    } else {
      filters.type = 'FARMER';
    }

    if (this.selectedStatus !== 'All') {
      filters.status = this.selectedStatus;
    }

    if (this.selectedRegion !== 'All') {
      filters.region = this.selectedRegion;
    }

    if (this.searchQuery.trim()) {
      filters.searchTerm = this.searchQuery.trim();
    }

    this.userService.getUsers(this.currentPage - 1, this.pageSize, filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          const allUsers = (response.content || []).map((user: any) => this.transformUserToMember(user));
          this.members = allUsers;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
          this.applyLocalFilters();
        },
        error: (error) => {
          console.error('Error loading members:', error);
          this.errorMessage = 'Failed to load members. Please check your connection.';
          this.isLoading = false;
        }
      });
  }


  // ===== QR CODE FUNCTIONALITY =====

  /**
   * Generate QR code for farmer account access
   */
  generateAccountAccessQR(member: Member) {
    console.log('🔲 Generating LOGIN QR code for member:', member.id);
    this.qrCodeLoading = true;
    this.showQRModal = true;
    this.selectedQRCode = null; // Clear previous so old image doesn't flash

    this.qrCodeService.generateLoginQRCode(member.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('✅ Login QR code generated for', member.id, '| Data:', response.qrCodeData?.substring(0, 30));
          this.selectedQRCode = { ...response, purpose: 'LOGIN' };
          this.qrCodeLoading = false;
        },
        error: (error) => {
          console.error('❌ Login QR code generation failed for', member.id, ':', error);
          // Fallback: generate QR data locally with LOGIN purpose
          const timestamp = Date.now();
          const signature = (Math.abs((member.id + timestamp).split('').reduce((a, c) => a + c.charCodeAt(0), 0)) % 0xFFFFFF).toString(16).padStart(6, '0');
          const qrData = `USER:${member.id}:LOGIN:${timestamp}:${signature}`;
          console.log('🔄 Fallback QR data for', member.id, ':', qrData);
          this.selectedQRCode = {
            userId: member.id,
            purpose: 'LOGIN',
            qrCodeImage: '', // No image in fallback
            qrCodeData: qrData,
            downloadUrl: undefined,
            size: 250,
            format: 'PNG',
            expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString() // 30 min
          };
          this.qrCodeLoading = false;
        }
      });
  }

  /**
   * Generate QR code for farmer registration
   */
  generateRegistrationQR(member?: Member) {
    console.log('🔲 Generating registration QR code');
    this.qrCodeLoading = true;
    this.showQRModal = true;

    // For registration QR, we use a generic user ID since it's for general registration
    const genericUserId = 'registration-general';

    this.qrCodeService.generateRegistrationQRCode(genericUserId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('✅ Registration QR code generated successfully');
          this.selectedQRCode = { ...response, purpose: 'REGISTRATION' };
          this.qrCodeLoading = false;
        },
        error: (error) => {
          console.error('❌ Registration QR code generation failed:', error);
          // Show fallback QR code data even if image generation fails
          const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(); // 30 days from now
          this.selectedQRCode = {
            userId: genericUserId,
            purpose: 'REGISTRATION',
            qrCodeImage: '', // Empty image
            qrCodeData: `REGISTRATION:COOPERATIVE:${Date.now()}`,
            downloadUrl: undefined,
            size: 250,
            format: 'PNG',
            expiresAt: expiresAt
          };
          this.qrCodeLoading = false;
          alert('QR code generation failed, but you can still see the registration data.');
        }
      });
  }

  /**
   * Download QR code as image
   */
  downloadQRCode() {
    if (!this.selectedQRCode) return;

    console.log('⬇️ Downloading QR code for user:', this.selectedQRCode.userId);

    this.qrCodeService.downloadQRCode(this.selectedQRCode.userId, 'REGISTRATION')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob) => {
          const filename = `qrcode_${this.selectedQRCode!.userId}_registration.png`;
          this.qrCodeService.triggerDownload(blob, filename);
          console.log('✅ QR code downloaded successfully');
        },
        error: (error) => {
          console.error('❌ QR code download failed:', error);
          alert('Failed to download QR code. Please try again.');
        }
      });
  }

  /**
   * Close QR code modal
   */
  closeQRModal() {
    this.showQRModal = false;
    this.selectedQRCode = null;
    this.qrCodeLoading = false;
  }

  // ===== EXPORT FUNCTIONALITY =====

  /**
   * Export members data in CSV format (Excel compatible)
   */
  onExport() {
    console.log('📤 Exporting members data to CSV...');
    this.exportLoading = true;

    try {
      // Create CSV data from current filtered members
      const headers = ['AR Code', 'Registration Number', 'Name', 'Phone', 'Type', 'Region', 'Farm Location', 'Primary Crop', 'Status', 'Email'];

      const csvData = this.filteredMembers.map(member => [
        member.id,
        member.registrationNumber || 'N/A',
        member.name,
        member.phone,
        member.type,
        member.region,
        member.gpsCoordinates || 'Not set',
        member.primaryCrop,
        member.status,
        member.email || 'N/A'
      ]);

      // Generate filename with timestamp
      const timestamp = new Date().toISOString().split('T')[0];
      const filename = `members_export_${timestamp}.csv`;

      // Create and download CSV file
      this.createCSVFile([headers, ...csvData], filename);

      this.exportLoading = false;
      alert(`✅ CSV export completed! Downloaded ${csvData.length} records.`);

    } catch (error) {
      console.error('❌ CSV export generation failed:', error);
      this.exportLoading = false;
      alert('Failed to generate CSV export. Please try again.');
    }
  }

  /**
   * Create CSV file from data
   */
  private createCSVFile(data: any[][], filename: string) {
    const csvContent = data.map(row =>
      row.map(cell => {
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        const cellStr = String(cell || '');
        if (cellStr.includes(',') || cellStr.includes('"') || cellStr.includes('\n')) {
          return '"' + cellStr.replace(/"/g, '""') + '"';
        }
        return cellStr;
      }).join(',')
    ).join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    this.exportService.triggerDownload(blob, filename);
  }

  // ===== EXISTING METHODS =====

  onMemberAdded(newMember: any) {
    console.log('➕ Member added event received:', newMember);

    const createCommand: any = {
      type: newMember.type,
      name: newMember.name,
      email: newMember.email || null,
      phoneNumber: newMember.phoneNumber,
      region: newMember.region || null,
      department: newMember.department || null,
      district: newMember.district || null,
      village: newMember.village || null,
      gpsCoordinates: newMember.gpsCoordinates || null,
      preferredLanguage: newMember.preferredLanguage || 'fr'
    };

    if (newMember.type === 'FARMER') {
      createCommand.agriculturalType = newMember.agriculturalType || null;
      createCommand.cropTypes = newMember.cropTypes || [];
      createCommand.livestockTypes = newMember.livestockTypes || [];
      createCommand.landArea = newMember.landArea || null;
      const coopUid = this.cooperativeUserIdForScope();
      if (coopUid) {
        createCommand.cooperativeUserId = coopUid;
      }
    }

    if (newMember.type === 'COOPERATIVE') {
      createCommand.cooperativeType = newMember.cooperativeType || null;
      createCommand.legalRegistrationNumber = newMember.legalRegistrationNumber || null;
      createCommand.establishmentYear = newMember.establishmentYear || null;
      createCommand.contactPerson = newMember.contactPerson || null;
    }

    console.log('🚀 Sending createUser command:', createCommand);
    this.isLoading = true;

    this.userService.createUser(createCommand)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: any) => {
          console.log('✅ Member created successfully:', user);
          alert(`✅ ${user.name} registered successfully!`);
          this.loadMembers();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('❌ Error creating member:', error);
          this.errorMessage = this.formatErrorMessage(error);
          this.isLoading = false;
          alert('❌ ' + this.errorMessage);
        }
      });
  }

  private formatErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }

    const statusMessages: { [key: number]: string } = {
      0: 'Cannot connect to server. Please check your internet connection.',
      400: 'Invalid data provided. Please check all required fields.',
      401: 'Your session has expired. Please log in again.',
      403: 'You do not have permission to perform this action.',
      404: 'Resource not found.',
      409: 'Phone number or email already exists.',
      500: 'Server error. Please try again later.'
    };

    return statusMessages[error.status] || 'An unexpected error occurred. Please try again.';
  }

  // ... rest of existing methods (transformUserToMember, pagination, filters, etc.)

  transformUserToMember(user: any): Member {
    // Extract region from user data - try multiple sources
    const region = user.region || (user.address && user.address.region);
    const gpsCoordinates = user.gpsCoordinates || (user.address && user.address.gpsCoordinates);

    return {
      id: user.userId || user.id,
      name: user.name,
      phone: user.phoneNumber,
      type: this.formatUserType(user.type),
      region: this.formatRegionName(region),
      gpsCoordinates: gpsCoordinates,
      primaryCrop: this.getPrimaryCrop(user),
      status: this.formatStatus(user.status),
      email: user.email,
      registrationNumber: user.registrationNumber
    };
  }

  getPrimaryCrop(user: any): string {
    if (user.farmerDetails?.cropTypes && user.farmerDetails.cropTypes.length > 0) {
      return this.formatCropName(user.farmerDetails.cropTypes[0]);
    }
    if (user.cooperativeDetails) {
      return 'Mixed';
    }
    return 'N/A';
  }

  formatCropName(crop: string): string {
    const cropMap: { [key: string]: string } = {
      'COCOA': 'Cocoa',
      'COFFEE': 'Coffee',
      'MAIZE': 'Maize',
      'CASSAVA': 'Cassava',
      'RICE': 'Rice',
      'COTTON': 'Cotton',
      'PALM_OIL': 'Palm Oil'
    };
    return cropMap[crop] || crop;
  }

  formatRegionName(region: string): string {
    if (!region) return 'N/A';
    const regionMap: { [key: string]: string } = {
      'ADAMAOUA': 'Adamaoua',
      'CENTRE': 'Centre',
      'EST': 'East',
      'EXTREME_NORD': 'Far North',
      'LITTORAL': 'Littoral',
      'NORD': 'North',
      'NORD_OUEST': 'Northwest',
      'OUEST': 'West',
      'SUD': 'South',
      'SUD_OUEST': 'Southwest'
    };
    return regionMap[region] || region;
  }

  formatUserType(type: string): string {
    const typeMap: { [key: string]: string } = {
      'FARMER': 'Farmer',
      'COOPERATIVE': 'Cooperative',
      'COOPERATIVE_MANAGER': 'Cooperative Manager',
      'GOVERNMENT': 'Government'
    };
    return typeMap[type] || type;
  }

  formatStatus(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'Active',
      'INACTIVE': 'Inactive',
      'PENDING': 'Pending',
      'SUSPENDED': 'Suspended'
    };
    return statusMap[status] || status;
  }

  formatRole(role: string): string {
    const roleMap: { [key: string]: string } = {
      'COOPERATIVE': 'Cooperative Manager',
      'FARMER': 'Farmer',
      'GOVERNMENT': 'Government Official'
    };
    return roleMap[role] || role;
  }

  getInitials(name: string): string {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  applyLocalFilters() {
    this.filteredMembers = this.members.filter(member => {
      const matchesStatus = this.selectedStatus === 'All' || member.status === this.selectedStatus;
      const matchesRegion = this.selectedRegion === 'All' || member.region === this.selectedRegion;
      // Fix crop filtering by comparing formatted crop names
      const matchesCrop = this.selectedCrop === 'All' ||
        this.formatCropName(this.selectedCrop) === member.primaryCrop ||
        member.primaryCrop === 'N/A' && this.selectedCrop === 'All';
      const matchesSearch = !this.searchQuery.trim() ||
        member.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        member.phone.includes(this.searchQuery) ||
        (member.registrationNumber && member.registrationNumber.includes(this.searchQuery));

      return matchesStatus && matchesRegion && matchesCrop && matchesSearch;
    });
    this.updatePaginatedMembers();
  }

  updatePaginatedMembers() {
    this.paginatedMembers = this.filteredMembers;
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadMembers();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadMembers();
    }
  }

  onStatusChange() {
    this.currentPage = 1;
    this.loadMembers();
  }

  onRegionChange() {
    this.currentPage = 1;
    this.loadMembers();
  }

  onCropChange() {
    this.currentPage = 1;
    this.applyLocalFilters();
  }

  onSearchChange() {
    this.currentPage = 1;
    this.loadMembers();
  }

  openAddMemberModal() {
    if (this.memberFormComponent) {
      this.memberFormComponent.openModal();
    }
  }

  openEditMemberModal(member: Member) {
    if (this.editMemberFormComponent) {
      this.editMemberFormComponent.openModal(member);
    }
  }

  openViewMemberModal(member: Member) {
    if (this.memberDetailComponent) {
      this.memberDetailComponent.openModal(member);
    }
  }

  openDeleteConfirmModal(member: Member) {
    if (this.deleteMemberComponent) {
      this.deleteMemberComponent.openModal(member);
    }
  }

  onMemberUpdated(updatedMember: any) {
    console.log('Member updated:', updatedMember);
    this.loadMembers();
    this.showSuccessMessage('Member updated successfully!');
  }

  onMemberDeleted(member: Member) {
    console.log('Member deleted:', member);
    this.userService.deleteUser(member.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loadMembers();
          this.showSuccessMessage('Member deleted successfully!');
        },
        error: (error) => {
          this.showErrorMessage('Failed to delete member');
        }
      });
  }

  onModalClosed() {
    console.log('Modal closed');
  }

  getTotalMembers(): number {
    return this.totalElements;
  }

  getFarmersCount(): number {
    return this.members.filter(m => m.type === 'Farmer').length;
  }

  getFarmersPercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getFarmersCount() / this.totalElements) * 100);
  }

  getCooperativesCount(): number {
    return this.members.filter(m => m.type === 'Cooperative').length;
  }

  getCooperativesPercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getCooperativesCount() / this.totalElements) * 100);
  }

  getActiveMembers(): number {
    return this.members.filter(m => m.status === 'Active').length;
  }

  getActivePercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getActiveMembers() / this.totalElements) * 100);
  }

  getTypeClass(type: string): string {
    return type === 'Farmer' ? 'type-farmer' : 'type-cooperative';
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'Active': 'status-active',
      'Inactive': 'status-inactive',
      'Pending': 'status-pending',
      'Suspended': 'status-suspended'
    };
    return statusMap[status] || 'status-active';
  }

  showSuccessMessage(message: string) {
    alert(message);
  }

  showErrorMessage(message: string) {
    alert('Error: ' + message);
  }
}