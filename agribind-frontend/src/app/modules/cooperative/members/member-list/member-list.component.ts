import { Component, OnInit } from '@angular/core';
import {CommonModule} from '@angular/common';

interface Member {
  id: string;
  name: string;
  phone: string;
  type: string;
  region: string;
  primaryCrop: string;
  status: string;
}

@Component({
  selector: 'app-member-list',
  templateUrl: './member-list.component.html',
  styleUrls: ['./member-list.component.scss'],
  standalone: true,
  imports: [CommonModule]
})
export class MemberListComponent implements OnInit {
  members: Member[] = [
    {
      id: 'COOP-001',
      name: 'John Farmer',
      phone: '+237 6XX XXX XXX',
      type: 'Farmer',
      region: 'North West',
      primaryCrop: 'Coffee',
      status: 'Active'
    },
    {
      id: 'COOP-002',
      name: 'Alice Cooper',
      phone: '+237 6XX XXX XXX',
      type: 'Operative',
      region: 'Littoral',
      primaryCrop: 'N/A',
      status: 'Active'
    },
    {
      id: 'COOP-003',
      name: 'Samuel Government',
      phone: '+237 6XX XXX XXX',
      type: 'Government Official',
      region: 'South West',
      primaryCrop: 'N/A',
      status: 'Pending'
    }
  ];

  ngOnInit(): void {}

  getInitials(name: string): string {
    if (!name) return '';
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
  }
}
