import { Component, EventEmitter, Output, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Member {
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
  lastProduction?: string;
  creditStatus?: string;
  farmLocation?: string;
}

@Component({
  selector: 'app-member-detail',
  templateUrl: './member-detail.component.html',
  styleUrls: ['./member-detail.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class MemberDetailComponent implements OnInit {
  @Output() modalClosed = new EventEmitter<void>();
  @Input() member: Member | null = null;

  isModalOpen = false;

  ngOnInit(): void {
    console.log('MemberDetailComponent initialized');
  }

  openModal(member: Member): void {
    console.log('Opening member detail modal for:', member);
    this.member = member;
    this.isModalOpen = true;
    document.body.style.overflow = 'hidden';
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.member = null;
    document.body.style.overflow = 'auto';
    this.modalClosed.emit();
  }

  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'active': return 'status-active';
      case 'inactive': return 'status-inactive';
      case 'pending': return 'status-pending';
      default: return 'status-active';
    }
  }

  getMemberTypeIcon(type: string): string {
    switch (type.toLowerCase()) {
      case 'farmer': return '👨‍🌾';
      case 'cooperative': return '🏢';
      default: return '👤';
    }
  }

  getCreditStatusClass(creditStatus: string | undefined): string {
    if (!creditStatus) return '';

    switch (creditStatus.toLowerCase()) {
      case 'good': return 'credit-good';
      case 'excellent': return 'credit-good';
      case 'fair': return 'credit-fair';
      case 'average': return 'credit-fair';
      case 'poor': return 'credit-poor';
      case 'bad': return 'credit-poor';
      default: return '';
    }
  }

  hasAdditionalInfo(): boolean {
    return !!(this.member?.farmSize || this.member?.lastProduction || this.member?.creditStatus);
  }
}
