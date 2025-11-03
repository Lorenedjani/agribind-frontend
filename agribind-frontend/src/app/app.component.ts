import { Component, ViewChild } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MemberFormComponent } from './modules/cooperative/members/member-form/member-form.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [RouterOutlet, CommonModule, MemberFormComponent]
})
export class AppComponent {
  title = 'agribind-frontend';

  @ViewChild(MemberFormComponent) memberFormComponent!: MemberFormComponent;

  // Your existing members data
  members: any[] = [
    // Your existing members data here
  ];

  // Method to open the modal
  openAddMemberModal(): void {
    this.memberFormComponent.openModal();
  }

  // Handle new member
  onMemberAdded(newMember: any): void {
    this.members.unshift(newMember);
    this.updateStats();
  }

  // Handle modal close
  onModalClosed(): void {
    // Any cleanup if needed
  }

  private updateStats(): void {
    // Update your statistics here
    console.log('Stats updated with new member');
  }
}
