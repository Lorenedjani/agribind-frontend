// src/app/modules/cooperative/members/member-list/member-list.component.ts
import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { ViewEncapsulation } from '@angular/core';

// Services
import { UserService, User, PageResponse, CreateUserCommand } from '../../../../core/services/user.service';
import { AuthService, UserDisplayInfo } from '../../../../core/services/auth.service';

// Import child components
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from "../../../../../shared/cooperative-sidebar/cooperative-sidebar.component";

// Member interface
interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  primaryCrop: string;
  status: string;
  email?: string;
  joinDate?: string;
  farmSize?: string;
  address?: string;
  farmLocation?: string;
  lastProduction?: string;
  creditStatus?: string;
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
  styleUrls: ['./member-list.component.scss'],
  encapsulation: ViewEncapsulation.None
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
  regionOptions = ['All', 'North West', 'South West', 'Littoral', 'Centre', 'West', 'Far North'];
  cropOptions = ['All', 'Cocoa', 'Coffee', 'Cassava', 'Maize', 'Rice', 'Cotton', 'Palm Oil'];

  // User info (for top bar) - now properly typed
  user: UserDisplayInfo = {
    name: 'Loading...',
    role: 'User',
    initials: 'U',
    email: ''
  };

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

  /**
   * Load current user information from AuthService
   */
  loadUserInfo() {
    // Subscribe to user display info observable
    this.authService.userDisplayInfo$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (userInfo) => {
          if (userInfo) {
            this.user = userInfo;
            console.log('✅ User info loaded:', this.user);
          }
        },
        error: (error) => {
          console.error('❌ Error loading user info:', error);
          // Fallback to synchronous method
          this.user = this.authService.getUserDisplayInfo();
        }
      });

    // Also get it synchronously for immediate display
    const syncUser = this.authService.getUserDisplayInfo();
    if (syncUser) {
      this.user = syncUser;
    }
  }

  /**
   * Load members from backend API
   */
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

    const apiPage = this.currentPage - 1;

    console.log('Loading members with filters:', filters);

    this.userService.getUsers(apiPage, this.pageSize, filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: PageResponse<User>) => {
          console.log('✅ Members loaded successfully:', response);

          this.members = response.content.map(user => this.transformUserToMember(user));
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.isLoading = false;

          this.applyLocalFilters();
        },
        error: (error) => {
          console.error('❌ Error loading members:', error);
          this.errorMessage = error.message || 'Failed to load members. Please try again.';
          this.isLoading = false;

          this.loadMockDataAsFallback();
        }
      });
  }

  transformUserToMember(user: User): Member {
    return {
      id: user.userId || user.id || 'N/A',
      name: user.name,
      phone: user.phoneNumber || user.phone || 'N/A',
      type: this.formatUserType(user.type),
      region: user.region || 'N/A',
      primaryCrop: this.getPrimaryCrop(user),
      status: this.formatStatus(user.status),
      email: user.email,
      joinDate: user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A',
      farmSize: user.landArea ? `${user.landArea} hectares` : 'N/A',
      address: this.formatAddress(user),
      farmLocation: user.village || user.district || 'N/A',
      lastProduction: 'N/A',
      creditStatus: 'Good'
    };
  }

  getPrimaryCrop(user: User): string {
    if (user.cropTypes && user.cropTypes.length > 0) {
      return user.cropTypes[0];
    }
    return user.primaryCrop || 'N/A';
  }

  formatAddress(user: User): string {
    const parts = [user.village, user.district, user.region].filter(Boolean);
    return parts.length > 0 ? parts.join(', ') : 'N/A';
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
      'SUSPENDED': 'Suspended',
      'DELETED': 'Deleted'
    };
    return statusMap[status] || status;
  }

  applyLocalFilters() {
    this.filteredMembers = this.members.filter(member => {
      const matchesCrop = this.selectedCrop === 'All' ||
        member.primaryCrop === this.selectedCrop;

      return matchesCrop;
    });

    this.updatePaginatedMembers();
  }

  updatePaginatedMembers() {
    this.paginatedMembers = this.filteredMembers;
    console.log(`Showing ${this.paginatedMembers.length} members on page ${this.currentPage} of ${this.totalPages}`);
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

  onMemberAdded(newMember: any) {
    console.log('Member added:', newMember);

    const userType: CreateUserCommand['type'] = newMember.Type === 'Farmer' ? 'FARMER' : 'COOPERATIVE';

    const createCommand: CreateUserCommand = {
      type: userType,
      name: newMember.Name,
      email: newMember.email,
      phoneNumber: newMember.contact,
      region: newMember.location,
      preferredLanguage: 'en',
      agriculturalType: 'CROP',
      cropTypes: [newMember.primaryCrop],
      landArea: parseFloat(newMember.farmSize) || 0
    };

    this.isLoading = true;

    this.userService.createUser(createCommand)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: User) => {
          console.log('✅ Member created successfully:', user);
          this.isLoading = false;
          this.loadMembers();
          this.showSuccessMessage('Member added successfully!');
        },
        error: (error) => {
          console.error('❌ Error creating member:', error);
          this.errorMessage = error.message || 'Failed to add member';
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
  }

  onMemberUpdated(updatedMember: Member) {
    console.log('Member updated:', updatedMember);

    const updateCommand = {
      name: updatedMember.name,
      phoneNumber: updatedMember.phone,
      email: updatedMember.email,
      region: updatedMember.region
    };

    this.isLoading = true;

    this.userService.updateUser(updatedMember.id, updateCommand)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user: User) => {
          console.log('✅ Member updated successfully:', user);
          this.isLoading = false;
          this.loadMembers();
          this.showSuccessMessage('Member updated successfully!');
        },
        error: (error) => {
          console.error('❌ Error updating member:', error);
          this.errorMessage = error.message || 'Failed to update member';
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
  }

  onMemberDeleted(member: Member) {
    console.log('Member deleted:', member);

    this.isLoading = true;

    this.userService.deleteUser(member.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('✅ Member deleted successfully');
          this.isLoading = false;
          this.loadMembers();
          this.showSuccessMessage('Member deleted successfully!');
        },
        error: (error) => {
          console.error('❌ Error deleting member:', error);
          this.errorMessage = error.message || 'Failed to delete member';
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
  }

  onModalClosed() {
    console.log('Modal closed');
  }

  onExport() {
    console.log('Exporting members...');
    this.isLoading = true;

    this.userService.exportUsers('CSV', { type: 'FARMER' })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = `members_export_${new Date().getTime()}.csv`;
          link.click();
          window.URL.revokeObjectURL(url);

          this.isLoading = false;
          this.showSuccessMessage('Export completed successfully!');
        },
        error: (error) => {
          console.error('❌ Export failed:', error);
          this.errorMessage = error.message || 'Failed to export members';
          this.isLoading = false;
          this.showErrorMessage(this.errorMessage);
        }
      });
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

  loadMockDataAsFallback() {
    console.warn('⚠️ Loading mock data as fallback');

    this.members = [
      {
        id: 'M001',
        name: 'Kwame Osei',
        phone: '+237 654 123 456',
        type: 'Farmer',
        region: 'North West',
        primaryCrop: 'Cocoa',
        status: 'Active',
        email: 'kwame.osei@example.com',
        joinDate: '2023-01-15',
        farmSize: '5.2 hectares',
        address: 'Bambili, Bamenda',
        farmLocation: 'Mankon Town',
        lastProduction: '2.5 tons',
        creditStatus: 'Good'
      },
      {
        id: 'M002',
        name: 'Ama Boateng',
        phone: '+237 677 234 567',
        type: 'Farmer',
        region: 'Centre',
        primaryCrop: 'Coffee',
        status: 'Active',
        email: 'ama.boateng@example.com',
        joinDate: '2023-02-20',
        farmSize: '3.8 hectares',
        address: 'Efoulan, Yaoundé',
        farmLocation: 'Yaoundé Central',
        lastProduction: '1.8 tons',
        creditStatus: 'Excellent'
      }
    ];

    this.totalElements = this.members.length;
    this.totalPages = 1;
    this.applyLocalFilters();
  }
}