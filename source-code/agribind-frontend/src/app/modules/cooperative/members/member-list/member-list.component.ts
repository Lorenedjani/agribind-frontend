// src/app/modules/cooperative/members/member-list/member-list.component.ts
import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

// Services
import { UserService, CreateUserCommand } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';

// Components
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from "../../../../../shared/cooperative-sidebar/cooperative-sidebar.component";

interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  primaryCrop: string;
  status: string;
  email?: string;
  registrationNumber?: string;
  qrCodeUrl?: string;
}

interface QRCodeResponse {
  userId: string;
  purpose: string;
  qrCodeImage: string;
  qrCodeData: string;
  downloadUrl?: string;
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
    DeleteMemberComponent,
    CooperativeSidebarComponent
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
  private apiUrl = 'http://localhost:8080'; // API Gateway URL

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
  cropOptions = ['All', 'Cocoa', 'Coffee', 'Cassava', 'Maize', 'Rice', 'Cotton', 'Palm Oil'];

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

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadUserInfo();
    this.loadMembers();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
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

    const filters: any = {
      page: this.currentPage - 1,
      size: this.pageSize
    };

    if (this.selectedStatus !== 'All') {
      filters.status = this.selectedStatus;
    }

    if (this.selectedRegion !== 'All') {
      filters.region = this.selectedRegion;
    }

    if (this.searchQuery.trim()) {
      filters.searchTerm = this.searchQuery.trim();
    }

    this.userService.getUsers(filters.page, filters.size, filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          this.members = response.content.map((user: any) => this.transformUserToMember(user));
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
          this.applyLocalFilters();
        },
        error: (error) => {
          console.error('❌ Error loading members:', error);
          this.errorMessage = 'Failed to load members. Please try again.';
          this.isLoading = false;
        }
      });
  }

  onMemberAdded(newMember: any) {
    console.log('Member added:', newMember);

    // Transform to CreateUserCommand format
    const createCommand: CreateUserCommand = {
      type: newMember.type,
      name: newMember.name,
      email: newMember.email,
      phoneNumber: newMember.phoneNumber,
      region: this.parseRegion(newMember.region),
      department: newMember.department,
      district: newMember.district,
      village: newMember.village,
      preferredLanguage: newMember.preferredLanguage,
      agriculturalType: newMember.agriculturalType,
      cropTypes: newMember.cropTypes,
      landArea: newMember.landArea,
      cooperativeType: newMember.cooperativeType,
      legalRegistrationNumber: newMember.legalRegistrationNumber,
      establishmentYear: newMember.establishmentYear
    };

    this.isLoading = true;

    this.userService.createUser(createCommand)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: any) => {
          console.log('✅ Member created successfully:', user);

          // Show success message with registration details
          this.showSuccessWithQR(user);

          // Reload the list
          this.loadMembers();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('❌ Error creating member:', error);
          this.errorMessage = error.message || 'Failed to add member';
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
  }

  // Show success message with QR code option
  showSuccessWithQR(user: any) {
    const message = `
      ✅ Member created successfully!

      Registration Number: ${user.registrationNumber || 'N/A'}
      User ID: ${user.userId}

      Click OK to view and download the QR code.
    `;

    if (confirm(message)) {
      this.generateAndShowQRCode(user.userId);
    }
  }

  // Generate and display QR code
  generateAndShowQRCode(userId: string) {
    const url = `${this.apiUrl}/api/v1/qrcodes/${userId}/registration`;

    this.http.get<QRCodeResponse>(url)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (qrResponse) => {
          this.selectedQRCode = qrResponse;
          this.showQRModal = true;
        },
        error: (error) => {
          console.error('❌ Error generating QR code:', error);
          alert('Failed to generate QR code. Please try again later.');
        }
      });
  }

  // Handle QR code button click in table
  onQRCodeClick(member: Member) {
    this.generateAndShowQRCode(member.id);
  }

  // Download QR code
  downloadQRCode() {
    if (!this.selectedQRCode) return;

    // Convert base64 to blob
    const base64Data = this.selectedQRCode.qrCodeImage.replace(/^data:image\/png;base64,/, '');
    const byteCharacters = atob(base64Data);
    const byteNumbers = new Array(byteCharacters.length);

    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }

    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: 'image/png' });

    // Create download link
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `qr-code-${this.selectedQRCode.userId}.png`;
    link.click();

    window.URL.revokeObjectURL(url);
  }

  closeQRModal() {
    this.showQRModal = false;
    this.selectedQRCode = null;
  }

  // Other existing methods...
  transformUserToMember(user: any): Member {
    return {
      id: user.userId || user.id,
      name: user.name,
      phone: user.phoneNumber,
      type: this.formatUserType(user.type),
      region: user.region,
      primaryCrop: this.getPrimaryCrop(user),
      status: this.formatStatus(user.status),
      email: user.email,
      registrationNumber: user.registrationNumber
    };
  }

  getPrimaryCrop(user: any): string {
    if (user.farmerDetails?.cropTypes && user.farmerDetails.cropTypes.length > 0) {
      return user.farmerDetails.cropTypes[0];
    }
    return 'N/A';
  }

  formatUserType(type: string): string {
    const typeMap: { [key: string]: string } = {
      'FARMER': 'Farmer',
      'COOPERATIVE': 'Cooperative',
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

  parseRegion(region: string): any {
    const regionMap: any = {
      'ADAMAOUA': 'ADAMAOUA',
      'CENTRE': 'CENTRE',
      'EST': 'EST',
      'EXTREME_NORD': 'EXTREME_NORD',
      'LITTORAL': 'LITTORAL',
      'NORD': 'NORD',
      'NORD_OUEST': 'NORD_OUEST',
      'OUEST': 'OUEST',
      'SUD': 'SUD',
      'SUD_OUEST': 'SUD_OUEST'
    };
    return regionMap[region] || region;
  }

  applyLocalFilters() {
    this.filteredMembers = this.members.filter(member => {
      const matchesCrop = this.selectedCrop === 'All' || member.primaryCrop === this.selectedCrop;
      return matchesCrop;
    });
    this.updatePaginatedMembers();
  }

  updatePaginatedMembers() {
    this.paginatedMembers = this.filteredMembers;
  }

  // Navigation and filters
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

  // Modal openers
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

  // Event handlers
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

  onExport() {
    console.log('Exporting members...');
    // Implementation for export
  }

  // Statistics
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

  // Styling helpers
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