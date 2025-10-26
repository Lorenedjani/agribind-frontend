// members.component.ts
import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MemberFormComponent } from './member-form/member-form.component';

@Component({
  selector: 'app-members',
  templateUrl: './members.component.html',
  styleUrls: ['./members.component.scss'],
  standalone: true,
  imports: [CommonModule, MemberFormComponent] // Import MemberFormComponent here
})
export class MembersComponent {
  @ViewChild(MemberFormComponent) memberFormComponent!: MemberFormComponent;

  members: any[] = [
    // Your existing members data
  ];

  openAddMemberModal(): void {
    this.memberFormComponent.openModal();
  }

  onMemberAdded(newMember: any): void {
    this.members.unshift(newMember);
    this.updateStats();
  }

  onModalClosed(): void {
    // Any cleanup if needed
  }

  private updateStats(): void {
    console.log('Stats updated with new member');
  }
}
