// src/app/modules/cooperative/members/member-list/member-list.component.ts
import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';

// Services
import { UserService, CreateUserCommand } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';

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
    private authService: AuthService
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

    const filters: any = {};

    if (this.selectedStatus !== 'All') {
      filters.status = this.selectedStatus;
    }

    if (this.selectedRegion !== 'All') {
      filters.region = this.selectedRegion;
    }

    if (this.searchQuery.trim()) {
      filters.searchTerm = this.searchQuery.trim();
    }

    console.log('🔍 Loading members with filters:', filters);

    this.userService.getUsers(this.currentPage - 1, this.pageSize, filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('✅ Members loaded:', response);
          this.members = response.content.map((user: any) => this.transformUserToMember(user));
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;
          this.applyLocalFilters();
        },
        error: (error) => {
          console.error('❌ Error loading members:', error);
          this.errorMessage = 'Failed to load members. Please check your connection and try again.';
          this.isLoading = false;
        }
      });
  }

  onMemberAdded(newMember: any) {
    console.log('➕ Member added event received:', newMember);

    // Transform to CreateUserCommand format
    const createCommand: CreateUserCommand = {
      type: newMember.type,
      name: newMember.name,
      email: newMember.email,
      phoneNumber: newMember.phoneNumber,
      region: newMember.region,
      department: newMember.department,
      district: newMember.district,
      village: newMember.village,
      gpsCoordinates: newMember.gpsCoordinates, // Farm GPS coordinates
      preferredLanguage: newMember.preferredLanguage,
      agriculturalType: newMember.agriculturalType,
      cropTypes: newMember.cropTypes,
      landArea: newMember.landArea,
      cooperativeType: newMember.cooperativeType,
      legalRegistrationNumber: newMember.legalRegistrationNumber,
      establishmentYear: newMember.establishmentYear,
      contactPerson: newMember.contactPerson
    };

    console.log('🚀 Sending createUser command:', createCommand);
    this.isLoading = true;

    this.userService.createUser(createCommand)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: any) => {
          console.log('✅ Member created successfully:', user);
          this.showSuccessMessage(`Member ${user.name} created successfully!`);

          // Show QR code if available
          if (user.registrationNumber) {
            this.showSuccessWithQR(user);
          }

          // Reload the list
          this.loadMembers();
          this.isLoading = false;
        },
        error: (error) => {
          console.error('❌ Error creating member:', error);
          this.errorMessage = this.formatErrorMessage(error);
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
  }

  formatErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.message) {
      return error.message;
    }
    if (error.status === 403) {
      return 'You do not have permission to perform this action.';
    }
    if (error.status === 401) {
      return 'Your session has expired. Please log in again.';
    }
    if (error.status === 400) {
      return 'Invalid data provided. Please check your input.';
    }
    return 'Failed to create member. Please try again.';
  }

  showSuccessWithQR(user: any) {
    const message = `✅ Member created successfully!\n\nRegistration Number: ${user.registrationNumber || 'N/A'}\nUser ID: ${user.userId}\n\nWould you like to view and download the QR code?`;

    if (confirm(message)) {
      this.userService.getUserById(user.userId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (fullUser: any) => {
            // Generate QR code logic here if needed
            console.log('User details for QR:', fullUser);
          },
          error: (err) => console.error('Error fetching user details:', err)
        });
    }
  }

  transformUserToMember(user: any): Member {
    return {
      id: user.userId || user.id,
      name: user.name,
      phone: user.phoneNumber,
      type: this.formatUserType(user.type),
      region: this.formatRegionName(user.region),
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
      'PALM_OIL': 'Palm Oil',
      'PLANTAINS': 'Plantains',
      'BANANAS': 'Bananas',
      'BEANS': 'Beans'
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
      const matchesCrop = this.selectedCrop === 'All' || member.primaryCrop === this.selectedCrop;
      return matchesCrop;
    });
    this.updatePaginatedMembers();
  }

  updatePaginatedMembers() {
    this.paginatedMembers = this.filteredMembers;
  }

  // Navigation
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

  // Filters
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
    this.userService.exportUsers('CSV', {
      status: this.selectedStatus !== 'All' ? this.selectedStatus : undefined,
      region: this.selectedRegion !== 'All' ? this.selectedRegion : undefined
    }).pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `members_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => console.error('Export failed:', error)
    });
  }

  // QR Code
  onQRCodeClick(member: Member) {
    console.log('Generate QR for:', member);
    // Implement QR generation
  }

  closeQRModal() {
    this.showQRModal = false;
    this.selectedQRCode = null;
  }

  downloadQRCode() {
    // Implement download
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
