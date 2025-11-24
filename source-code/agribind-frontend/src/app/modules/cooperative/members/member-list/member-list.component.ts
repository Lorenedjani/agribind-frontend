// src/app/modules/cooperative/members/member-list/member-list.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, User, PageResponse } from '../../../../core/services/user.service';

// Import child components
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from "../../../../../shared/cooperative-sidebar/cooperative-sidebar.component";

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
export class MemberListComponent implements OnInit {
  members: User[] = [];
  filteredMembers: User[] = [];
  paginatedMembers: User[] = [];
  isLoading = false;
  errorMessage = '';

  // Pagination
  currentPage = 1; // Changed from 0 to 1 to match template
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Search and Filters
  searchQuery = '';
  selectedStatus = 'All';
  selectedRegion = 'All';
  selectedCrop = 'All';

  // Filter options
  statusOptions = ['All', 'Active', 'Inactive', 'Pending'];
  regionOptions = ['All', 'Centre', 'Littoral', 'West', 'North', 'South'];
  cropOptions = ['All', 'Cocoa', 'Coffee', 'Cassava', 'Maize', 'Rice'];

  // Modal states
  selectedMember: User | null = null;

  // User info (for top bar)
  user = {
    name: 'Admin User',
    role: 'Administrator',
    initials: 'AU'
  };

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadMembers();
  }

  /**
   * Load members from API
   */
  loadMembers() {
    this.isLoading = true;
    this.errorMessage = '';

    const filters = {
      type: 'FARMER' // Only load farmers (members)
    };

    // Convert page number (1-based for UI, 0-based for API)
    const apiPage = this.currentPage - 1;

    this.userService.getUsers(apiPage, this.pageSize, filters).subscribe({
      next: (response: PageResponse<User>) => {
        this.members = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.isLoading = false;
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error loading members:', error);
        this.errorMessage = 'Failed to load members. Please try again.';
        this.isLoading = false;
      }
    });
  }

  /**
   * Apply search and filters
   */
  applyFilters() {
    this.filteredMembers = this.members.filter(member => {
      // Search filter
      const matchesSearch = !this.searchQuery ||
        member.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        member.phoneNumber.includes(this.searchQuery) ||
        (member.email && member.email.toLowerCase().includes(this.searchQuery.toLowerCase()));

      // Status filter
      const matchesStatus = this.selectedStatus === 'All' ||
        member.status.toLowerCase() === this.selectedStatus.toLowerCase();

      // Region filter
      const matchesRegion = this.selectedRegion === 'All' ||
        member.region === this.selectedRegion;

      // Crop filter (assuming cropTypes is an array)
      const matchesCrop = this.selectedCrop === 'All' ||
        (member.cropTypes && member.cropTypes.includes(this.selectedCrop));

      return matchesSearch && matchesStatus && matchesRegion && matchesCrop;
    });

    this.updatePaginatedMembers();
  }

  /**
   * Update paginated members for current view
   */
  updatePaginatedMembers() {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedMembers = this.filteredMembers.slice(startIndex, endIndex);

    // Update total pages based on filtered results
    this.totalPages = Math.ceil(this.filteredMembers.length / this.pageSize) || 1;

    // Adjust current page if needed
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
      this.updatePaginatedMembers();
    }
  }

  /**
   * Pagination methods
   */
  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePaginatedMembers();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePaginatedMembers();
    }
  }

  /**
   * Modal handlers
   */
  openAddMemberModal() {
    // This will be handled by the child component
    console.log('Opening add member modal');
  }

  openEditMemberModal(member: User) {
    this.selectedMember = member;
    // Child component will handle the modal
  }

  openViewMemberModal(member: User) {
    this.selectedMember = member;
    // Child component will handle the modal
  }

  openDeleteConfirmModal(member: User) {
    this.selectedMember = member;
    // Child component will handle the modal
  }

  /**
   * Event handlers from child components
   */
  onMemberAdded(member: User) {
    console.log('Member added:', member);
    this.loadMembers();
  }

  onMemberUpdated(member: User) {
    console.log('Member updated:', member);
    this.loadMembers();
  }

  onMemberDeleted(memberId: string) {
    console.log('Member deleted:', memberId);
    this.loadMembers();
  }

  onModalClosed() {
    this.selectedMember = null;
  }

  /**
   * Export functionality
   */
  onExport() {
    this.isLoading = true;

    this.userService.exportUsers('EXCEL', { type: 'FARMER' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `members_export_${new Date().getTime()}.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.isLoading = false;
        console.log('✅ Export successful');
      },
      error: (error) => {
        console.error('❌ Export failed:', error);
        this.errorMessage = 'Failed to export members. Please try again.';
        this.isLoading = false;
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
    return this.members.filter(m => m.type === 'FARMER').length;
  }

  getFarmersPercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getFarmersCount() / this.totalElements) * 100);
  }

  getCooperativesCount(): number {
    return this.members.filter(m => m.type === 'COOPERATIVE').length;
  }

  getCooperativesPercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getCooperativesCount() / this.totalElements) * 100);
  }

  getActiveMembers(): number {
    return this.members.filter(m => m.status === 'ACTIVE').length;
  }

  getActivePercentage(): number {
    if (this.totalElements === 0) return 0;
    return Math.round((this.getActiveMembers() / this.totalElements) * 100);
  }

  /**
   * Helper methods for template
   */
  getTypeClass(type: string): string {
    const typeMap: { [key: string]: string } = {
      'FARMER': 'badge-farmer',
      'COOPERATIVE': 'badge-cooperative',
      'ADMIN': 'badge-admin'
    };
    return typeMap[type] || 'badge-default';
  }

  getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'ACTIVE': 'status-active',
      'INACTIVE': 'status-inactive',
      'PENDING': 'status-pending',
      'SUSPENDED': 'status-suspended'
    };
    return statusMap[status.toUpperCase()] || 'status-default';
  }
}