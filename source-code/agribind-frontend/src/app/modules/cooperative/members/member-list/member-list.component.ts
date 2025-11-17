import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from '../../../../../shared/cooperative-sidebar/cooperative-sidebar.component';
import { UserService, User, PageResponse } from '../../../../core/services/user.service';

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
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss'],
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
  encapsulation: ViewEncapsulation.None
})
export class MemberListComponent implements OnInit {
  @ViewChild(MemberFormComponent) memberFormComponent!: MemberFormComponent;
  @ViewChild(EditMemberFormComponent) editMemberFormComponent!: EditMemberFormComponent;
  @ViewChild(MemberDetailComponent) memberDetailComponent!: MemberDetailComponent;
  @ViewChild(DeleteMemberComponent) deleteMemberComponent!: DeleteMemberComponent;

  // User info
  user = { name: 'Emmanuel Njoya', role: 'Manager', initials: 'EN' };

  // Search and filters
  searchQuery = '';
  selectedStatus = 'All Status';
  selectedRegion = 'All Regions';
  selectedCrop = 'All Crops';

  // Filter options
  statusOptions = ['All Status', 'Active', 'Inactive', 'Pending'];
  regionOptions = [
    'All Regions',
    'NORD_OUEST',
    'SUD_OUEST',
    'LITTORAL',
    'CENTRE',
    'NORD',
    'OUEST',
    'EXTREME_NORD',
    'ADAMAOUA',
    'SUD'
  ];
  cropOptions = [
    'All Crops',
    'Cocoa',
    'Coffee',
    'Maize',
    'Plantains',
    'Cotton',
    'Palm Oil',
    'Cassava',
    'Rice'
  ];

  // Data
  members: Member[] = [];
  loading = false;
  error = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;

  // Statistics
  totalMembers = 0;
  farmersCount = 0;
  cooperativesCount = 0;
  activeMembers = 0;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadMembers();
    this.loadStatistics();
  }

  loadMembers(): void {
    this.loading = true;
    this.error = '';

    const filters = this.buildFilters();

    this.userService.getUsers(this.currentPage, this.pageSize, filters).subscribe({
      next: (response: PageResponse<User>) => {
        this.members = this.mapUsersToMembers(response.content);
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.currentPage;
        this.loading = false;
        console.log('Loaded members from backend:', this.members.length);
      },
      error: (error) => {
        console.error('Error loading members:', error);
        this.error = 'Failed to load members. Please try again.';
        this.loading = false;
      }
    });
  }

  loadStatistics(): void {
    // Load total members
    this.userService.getUsers(0, 1, {}).subscribe({
      next: (response) => {
        this.totalMembers = response.totalElements;
      }
    });

    // Load farmers count
    this.userService.getFarmers(0, 1).subscribe({
      next: (response) => {
        this.farmersCount = response.totalElements;
      }
    });

    // Load cooperatives count
    this.userService.getCooperatives(0, 1).subscribe({
      next: (response) => {
        this.cooperativesCount = response.totalElements;
      }
    });

    // Load active members count
    this.userService.getUsers(0, 1, { status: 'ACTIVE' }).subscribe({
      next: (response) => {
        this.activeMembers = response.totalElements;
      }
    });
  }

  buildFilters(): any {
    const filters: any = {};

    if (this.searchQuery) {
      filters.searchTerm = this.searchQuery;
    }

    if (this.selectedStatus !== 'All Status') {
      filters.status = this.selectedStatus.toUpperCase();
    }

    if (this.selectedRegion !== 'All Regions') {
      filters.region = this.selectedRegion;
    }

    if (this.selectedCrop !== 'All Crops') {
      filters.primaryCrop = this.selectedCrop;
    }

    return filters;
  }

  mapUsersToMembers(users: User[]): Member[] {
    return users.map(user => ({
      id: user.userId || user.id || '',
      name: user.name,
      phone: user.phoneNumber,
      type: user.type,
      region: user.region || '',
      primaryCrop: this.getPrimaryCrop(user),
      status: user.status || 'Active',
      email: user.email,
      farmSize: user.landArea ? `${user.landArea} ha` : undefined,
      address: this.buildAddress(user),
      farmLocation: user.village || user.district || undefined
    }));
  }

  getPrimaryCrop(user: User): string {
    if (user.cropTypes && user.cropTypes.length > 0) {
      return user.cropTypes[0];
    }
    return 'N/A';
  }

  buildAddress(user: User): string {
    const parts = [user.village, user.district, user.department, user.region]
      .filter(Boolean);
    return parts.join(', ');
  }

  // Computed properties for statistics
  getTotalMembers(): number {
    return this.totalMembers;
  }

  getFarmersCount(): number {
    return this.farmersCount;
  }

  getCooperativesCount(): number {
    return this.cooperativesCount;
  }

  getActiveMembers(): number {
    return this.activeMembers;
  }

  getFarmersPercentage(): string {
    const percentage = (this.farmersCount / this.totalMembers) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  getCooperativesPercentage(): string {
    const percentage = (this.cooperativesCount / this.totalMembers) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  getActivePercentage(): string {
    const percentage = (this.activeMembers / this.totalMembers) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  // Filtering triggers
  onSearchChange(): void {
    this.currentPage = 0;
    this.loadMembers();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadMembers();
  }

  get filteredMembers(): Member[] {
    return this.members; // Backend handles filtering now
  }

  get paginatedMembers(): Member[] {
    return this.members; // Backend handles pagination now
  }

  // Pagination
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadMembers();
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadMembers();
    }
  }

  // Styling helpers
  getTypeClass(type: string): string {
    return type.toLowerCase() === 'farmer' ? 'type-farmer' : 'type-cooperative';
  }

  getStatusClass(status: string): string {
    return 'status-' + status.toLowerCase();
  }

  // Modal actions
  openAddMemberModal(): void {
    if (this.memberFormComponent) {
      this.memberFormComponent.openModal();
    }
  }

  openViewMemberModal(member: Member): void {
    if (this.memberDetailComponent) {
      this.memberDetailComponent.openModal(member);
    }
  }

  openEditMemberModal(member: Member): void {
    if (this.editMemberFormComponent) {
      this.editMemberFormComponent.openModal(member);
    }
  }

  openDeleteConfirmModal(member: Member): void {
    if (this.deleteMemberComponent) {
      this.deleteMemberComponent.openModal(member);
    }
  }

  // Export functionality
  onExport(): void {
    const filters = this.buildFilters();

    this.userService.exportUsers('EXCEL', filters).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `members_export_${new Date().toISOString()}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Export failed:', error);
        alert('Export failed. Please try again.');
      }
    });
  }

  // Event handlers
  onMemberAdded(newMember: any): void {
    console.log('Adding new member:', newMember);

    const user: User = {
      type: newMember.Type as 'FARMER' | 'COOPERATIVE' | 'GOVERNMENT',
      name: newMember.Name,
      phoneNumber: newMember.contact,
      email: newMember.email,
      region: newMember.location,
      department: newMember.address,
      village: newMember.farmLocation,
      preferredLanguage: 'en',
      agriculturalType: 'MIXED',
      cropTypes: [newMember.primaryCrop],
      landArea: parseFloat(newMember.farmSize) || 0
    };

    this.userService.createUser(user).subscribe({
      next: (response) => {
        console.log('Member created successfully:', response);
        this.loadMembers();
        this.loadStatistics();
        alert('Member added successfully!');
      },
      error: (error) => {
        console.error('Error creating member:', error);
        alert('Failed to add member. Please try again.');
      }
    });
  }

  onMemberUpdated(updatedMember: Member): void {
    console.log('Updating member:', updatedMember);

    const updates: Partial<User> = {
      name: updatedMember.name,
      phoneNumber: updatedMember.phone,
      email: updatedMember.email,
      status: updatedMember.status
    };

    this.userService.updateUser(updatedMember.id, updates).subscribe({
      next: (response) => {
        console.log('Member updated successfully:', response);
        this.loadMembers();
        alert('Member updated successfully!');
      },
      error: (error) => {
        console.error('Error updating member:', error);
        alert('Failed to update member. Please try again.');
      }
    });
  }

  onMemberDeleted(deletedMember: Member): void {
    console.log('Deleting member:', deletedMember);

    this.userService.deleteUser(deletedMember.id).subscribe({
      next: () => {
        console.log('Member deleted successfully');
        this.loadMembers();
        this.loadStatistics();
        alert(`Member ${deletedMember.name} has been deleted successfully.`);
      },
      error: (error) => {
        console.error('Error deleting member:', error);
        alert('Failed to delete member. Please try again.');
      }
    });
  }

  onModalClosed(): void {
    console.log('Modal closed');
  }

  onSidebarClose(): void {
    console.log('Sidebar closed');
  }
}
