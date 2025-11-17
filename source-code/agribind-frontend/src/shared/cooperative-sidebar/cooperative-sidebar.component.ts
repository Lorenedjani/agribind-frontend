import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-cooperative-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cooperative-sidebar.component.html',
  styleUrls: ['./cooperative-sidebar.component.scss']
})
export class CooperativeSidebarComponent {
  @Output() closeSidebar = new EventEmitter<void>();

  onClose(): void {
    this.closeSidebar.emit();
  }
}