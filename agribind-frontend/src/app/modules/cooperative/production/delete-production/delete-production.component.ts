import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Production } from '../production-list/production-list.component';

@Component({
  selector: 'app-delete-production',
  templateUrl: './delete-production.component.html',
  styleUrls: ['./delete-production.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class DeleteProductionComponent {
  @Input() production: Production | null = null;
  @Input() isModalOpen = false;
  @Output() productionDeleted = new EventEmitter<string>();
  @Output() modalClosed = new EventEmitter<void>();

  onConfirm(): void {
    if (this.production) {
      this.productionDeleted.emit(this.production.id);
    }
  }

  closeModal(): void {
    this.modalClosed.emit();
  }
}