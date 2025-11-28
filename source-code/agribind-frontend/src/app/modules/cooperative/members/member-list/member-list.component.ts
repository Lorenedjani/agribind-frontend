// src/app/modules/cooperative/members/member-list/member-list.component.ts
import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { ViewEncapsulation } from '@angular/core';

// Services
import { UserService, User, PageResponse } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';

// Import child components
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from "../../../../../shared/cooperative-sidebar/cooperative-sidebar.component";

// Simple Member interface for the component
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

  // User info (for top bar)
  user = {
    name: 'Admin User',
    role: 'Administrator',
    initials: 'AU'
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
   * Load current user information
   */
  loadUserInfo() {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.user = {
        name: currentUser.username || 'User',
        role: this.formatRole(currentUser.role),
        initials: this.getInitials(currentUser.username || 'User')
      };
    }
  }

  /**
   * Load members from backend API
   */
  loadMembers() {
    this.isLoading = true;
    this.errorMessage = '';

    // Build filters for API
    const filters: any = {};

    // Apply status filter
    if (this.selectedStatus !== 'All') {
      filters.status = this.selectedStatus;
    }

    // Apply region filter
    if (this.selectedRegion !== 'All') {
      filters.region = this.selectedRegion;
    }

    // Apply search query
    if (this.searchQuery.trim()) {
      filters.searchTerm = this.searchQuery.trim();
    }

    // Convert page number (1-based for UI, 0-based for API)
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

          // Load mock data as fallback
          this.loadMockDataAsFallback();
        }
      });
  }

  /**
   * Transform User from API to Member format
   */
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
      lastProduction: 'N/A', // This would come from production records
      creditStatus: 'Good' // This would come from credit service
    };
  }

  /**
   * Get primary crop from user
   */
  getPrimaryCrop(user: User): string {
    if (user.cropTypes && user.cropTypes.length > 0) {
      return user.cropTypes[0];
    }
    return user.primaryCrop || 'N/A';
  }

  /**
   * Format user address
   */
  formatAddress(user: User): string {
    const parts = [user.village, user.district, user.region].filter(Boolean);
    return parts.length > 0 ? parts.join(', ') : 'N/A';
  }

  /**
   * Format user type for display
   */
  formatUserType(type: string): string {
    const typeMap: { [key: string]: string } = {
      'FARMER': 'Farmer',
      'COOPERATIVE': 'Cooperative',
      'GOVERNMENT': 'Government'
    };
    return typeMap[type] || type;
  }

  /**
   * Format status for display
   */
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

  /**
   * Format role for display
   */
  formatRole(role: string): string {
    const roleMap: { [key: string]: string } = {
      'COOPERATIVE': 'Cooperative Manager',
      'FARMER': 'Farmer',
      'GOVERNMENT': 'Government Official'
    };
    return roleMap[role] || 'User';
  }

  /**
   * Get initials from name
   */
  getInitials(name: string): string {
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  /**
   * Apply local filters (for crop type which isn't in backend filter)
   */
  applyLocalFilters() {
    this.filteredMembers = this.members.filter(member => {
      // Crop filter (local only)
      const matchesCrop = this.selectedCrop === 'All' ||
        member.primaryCrop === this.selectedCrop;

      return matchesCrop;
    });

    this.updatePaginatedMembers();
  }

  /**
   * Update paginated members for current view
   */
  updatePaginatedMembers() {
    // Since we're using server-side pagination, just use the filtered members
    this.paginatedMembers = this.filteredMembers;

    // Update display count
    console.log(`Showing ${this.paginatedMembers.length} members on page ${this.currentPage} of ${this.totalPages}`);
  }

  /**
   * Pagination methods
   */
  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadMembers(); // Reload from server
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadMembers(); // Reload from server
    }
  }

  /**
   * Filter change handlers
   */
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
    // Debounce search
    this.currentPage = 1;
    this.loadMembers();
  }

  /**
   * Modal handlers
   */
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

  /**
   * Event handlers from child components
   */
  onMemberAdded(newMember: any) {
    console.log('Member added:', newMember);

    // Create user command for API
    const createCommand = {
      type: newMember.Type === 'Farmer' ? 'FARMER' : 'COOPERATIVE',
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
          this.loadMembers(); // Reload the list
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
          this.loadMembers(); // Reload the list
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
          this.loadMembers(); // Reload the list
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

  /**
   * Export functionality
   */
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

  /**
   * Statistics methods for cards
   */
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

  /**
   * Helper methods for template
   */
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

  /**
   * Show success message
   */
  showSuccessMessage(message: string) {
    // You can implement a toast notification service here
    alert(message);
  }

  /**
   * Show error message
   */
  showErrorMessage(message: string) {
    // You can implement a toast notification service here
    alert('Error: ' + message);
  }

  /**
   * Load mock data as fallback when backend is unavailable
   */
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