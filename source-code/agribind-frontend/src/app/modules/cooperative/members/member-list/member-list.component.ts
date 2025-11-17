import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';
import { MemberDetailComponent } from '../member-detail/member-detail.component';
import { DeleteMemberComponent } from '../delete-member/delete-member.component';
import { CooperativeSidebarComponent } from '../../../../../shared/cooperative-sidebar/cooperative-sidebar.component';


interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  primaryCrop: string;
  status: string;
  firstName?: string;
  lastName?: string;
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
  @ViewChild(MemberFormComponent, { static: false }) memberFormComponent!: MemberFormComponent;
  @ViewChild(EditMemberFormComponent, { static: false }) editMemberFormComponent!: EditMemberFormComponent;
  @ViewChild(MemberDetailComponent, { static: false }) memberDetailComponent!: MemberDetailComponent;
  @ViewChild(DeleteMemberComponent, { static: false }) deleteMemberComponent!: DeleteMemberComponent;

  // User info for header
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
    'North West',
    'South West',
    'Littoral',
    'Centre',
    'North',
    'West',
    'Far North',
    'Adamawa',
    'South'
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

  // Pagination
  currentPage = 1;
  pageSize = 10;

  members: Member[] = [
    {
      id: 'M001',
      name: 'Kwame Osei',
      phone: '+237 84234597',
      type: 'Farmer',
      region: 'North West',
      primaryCrop: 'Cocoa',
      status: 'Active'
    },
    {
      id: 'M002',
      name: 'Anna Boaleng',
      phone: '+237 78438887',
      type: 'Farmer',
      region: 'South West',
      primaryCrop: 'Coffee',
      status: 'Active'
    },
    {
      id: 'M003',
      name: 'Yann Mensah',
      phone: '+237 84284756',
      type: 'Farmer',
      region: 'Littoral',
      primaryCrop: 'Cocoa',
      status: 'Inactive'
    },
    {
      id: 'M004',
      name: 'Akosua Darko',
      phone: '+237 55609283',
      type: 'Cooperative',
      region: 'North West',
      primaryCrop: 'Cotton',
      status: 'Active'
    },
    {
      id: 'M005',
      name: 'Kofi Asante',
      phone: '+237 67890123',
      type: 'Farmer',
      region: 'Centre',
      primaryCrop: 'Maize',
      status: 'Active'
    },
    {
      id: 'M006',
      name: 'Abena Owusu',
      phone: '+237 78901234',
      type: 'Farmer',
      region: 'West',
      primaryCrop: 'Palm Oil',
      status: 'Pending'
    }
  ];

  ngOnInit(): void {
    console.log('MemberListComponent loaded with', this.members.length, 'members');
  }

  // Computed properties for statistics
  getTotalMembers(): number {
    return this.members.length;
  }

  getFarmersCount(): number {
    return this.members.filter(member => member.type === 'Farmer').length;
  }

  getCooperativesCount(): number {
    return this.members.filter(member => member.type === 'Cooperative').length;
  }

  getActiveMembers(): number {
    return this.members.filter(member => member.status === 'Active').length;
  }

  getFarmersPercentage(): string {
    const percentage = (this.getFarmersCount() / this.getTotalMembers()) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  getCooperativesPercentage(): string {
    const percentage = (this.getCooperativesCount() / this.getTotalMembers()) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  getActivePercentage(): string {
    const percentage = (this.getActiveMembers() / this.getTotalMembers()) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  // Filtering logic
  get filteredMembers(): Member[] {
    return this.members.filter(member => {
      const searchLower = this.searchQuery.toLowerCase();
      const matchSearch =
        !this.searchQuery ||
        member.id.toLowerCase().includes(searchLower) ||
        member.name.toLowerCase().includes(searchLower) ||
        member.type.toLowerCase().includes(searchLower);

      const matchStatus =
        this.selectedStatus === 'All Status' || member.status === this.selectedStatus;

      const matchRegion =
        this.selectedRegion === 'All Regions' || member.region === this.selectedRegion;

      const matchCrop =
        this.selectedCrop === 'All Crops' || member.primaryCrop === this.selectedCrop;

      return matchSearch && matchStatus && matchRegion && matchCrop;
    });
  }

  get totalPages(): number {
    return Math.ceil(this.filteredMembers.length / this.pageSize) || 1;
  }

  get paginatedMembers(): Member[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.filteredMembers.slice(startIndex, endIndex);
  }

  // Pagination methods
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  // Helper methods for styling
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
    } else {
      console.error('MemberFormComponent not found!');
    }
  }

  openViewMemberModal(member: Member): void {
    console.log('Opening view modal for member:', member);
    if (this.memberDetailComponent) {
      this.memberDetailComponent.openModal(member);
    } else {
      console.error('MemberDetailComponent not found!');
    }
  }

  openEditMemberModal(member: Member): void {
    if (this.editMemberFormComponent) {
      this.editMemberFormComponent.openModal(member);
    } else {
      console.error('EditMemberFormComponent not found!');
    }
  }

  openDeleteConfirmModal(member: Member): void {
    console.log('Opening delete confirmation for member:', member);
    if (this.deleteMemberComponent) {
      this.deleteMemberComponent.openModal(member);
    } else {
      console.error('DeleteMemberComponent not found!');
    }
  }

  // Export functionality
  onExport(): void {
    alert('Export functionality would be implemented here');
    // Similar to production page export logic
  }

  // Event handlers
  onMemberAdded(newMember: any): void {
    console.log('New member received:', newMember);

    const nextId = this.getNextMemberId();
    const convertedMember: Member = {
      id: nextId,
      name: newMember.Name,
      phone: newMember.contact,
      type: newMember.Type,
      region: newMember.location,
      primaryCrop: newMember.primaryCrop,
      status: newMember.status,
      email: newMember.email || '',
      joinDate: newMember.joinDate || new Date().toISOString().split('T')[0],
      farmSize: newMember.farmSize || '',
      address: newMember.address || '',
      farmLocation: newMember.farmLocation || '',
      lastProduction: newMember.lastProduction || '',
      creditStatus: newMember.creditStatus || ''
    };

    this.members.push(convertedMember);
    this.updateStats();
    console.log('Total members now:', this.members.length);
  }

  onMemberUpdated(updatedMember: Member): void {
    console.log('Member updated:', updatedMember);

    const index = this.members.findIndex(member => member.id === updatedMember.id);
    if (index !== -1) {
      this.members[index] = updatedMember;
      console.log('Member successfully updated');
    } else {
      console.error('Member not found for update');
    }
  }

  onMemberDeleted(deletedMember: Member): void {
    console.log('Member deleted:', deletedMember);
    this.deleteMember(deletedMember);
  }

  onModalClosed(): void {
    console.log('Modal closed');
  }

  // Private helper methods
  private getNextMemberId(): string {
    if (this.members.length === 0) {
      return 'M001';
    }

    const existingIds = this.members.map(member => {
      const numericPart = member.id.replace('M', '');
      return parseInt(numericPart, 10);
    });

    const maxId = Math.max(...existingIds);
    const nextId = maxId + 1;
    return 'M' + nextId.toString().padStart(3, '0');
  }

  private deleteMember(member: Member): void {
    const index = this.members.findIndex(m => m.id === member.id);
    if (index !== -1) {
      this.members.splice(index, 1);
      console.log('Member deleted successfully:', member.name);
      this.updateStats();

      // Show success message
      alert(`Member ${member.name} has been deleted successfully.`);
    }
  }

  private updateStats(): void {
    console.log('Stats updated with new member');
  }

  onSidebarClose(): void {
    // Add logic to hide sidebar, e.g., toggle a class or emit further
    console.log('Sidebar closed');
  }
}
