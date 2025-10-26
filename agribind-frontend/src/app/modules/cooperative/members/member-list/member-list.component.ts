// member-list.component.ts
import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MemberFormComponent } from '../member-form/member-form.component';
import { EditMemberFormComponent } from '../edit-member-form/edit-member-form.component';

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
}

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MemberFormComponent, EditMemberFormComponent]
})
export class MemberListComponent implements OnInit {
  @ViewChild(MemberFormComponent, { static: false }) memberFormComponent!: MemberFormComponent;
  @ViewChild(EditMemberFormComponent, { static: false }) editMemberFormComponent!: EditMemberFormComponent;

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
      status: 'Inactive'
    }
  ];

  ngOnInit(): void {
    console.log('MemberListComponent loaded with', this.members.length, 'members');
  }

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

  openAddMemberModal(): void {
    if (this.memberFormComponent) {
      this.memberFormComponent.openModal();
    } else {
      console.error('MemberFormComponent not found!');
    }
  }

  openEditMemberModal(member: Member): void {
    if (this.editMemberFormComponent) {
      this.editMemberFormComponent.openModal(member);
    } else {
      console.error('EditMemberFormComponent not found!');
    }
  }



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
      status: newMember.status
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

  onModalClosed(): void {
    console.log('Modal closed');
  }

  private updateStats(): void {
    console.log('Stats updated with new member');
  }

  getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  }

  getTotalMembers(): number {
    return this.members.length;
  }

  getFarmersCount(): number {
    return this.members.filter(member => member.type === 'Farmer').length;
  }

  getCooperativesCount(): number {
    return this.members.filter(member => member.type === 'Cooperative').length;
  }

  getFarmersPercentage(): string {
    const percentage = (this.getFarmersCount() / this.getTotalMembers()) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }

  getCooperativesPercentage(): string {
    const percentage = (this.getCooperativesCount() / this.getTotalMembers()) * 100;
    return isNaN(percentage) ? '0' : percentage.toFixed(1);
  }
}
