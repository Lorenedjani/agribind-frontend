import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Production } from '../production-list/production-list.component';

@Component({
  selector: 'app-production-detail',
  templateUrl: './production-detail.component.html',
  styleUrls: ['./production-detail.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class ProductionDetailComponent {
  @Input() production: Production | null = null;
  @Input() isModalOpen = false;
  @Output() modalClosed = new EventEmitter<void>();

  closeModal(): void {
    this.modalClosed.emit();
  }
}