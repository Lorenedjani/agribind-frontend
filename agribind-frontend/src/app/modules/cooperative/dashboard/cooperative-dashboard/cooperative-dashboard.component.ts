import { Component, OnInit } from '@angular/core';

interface Member {
  id: string;
  name: string;
  phone: string;
  region: string;
  primaryCrop: string;
  farmSize: number;
  lastProduction: string;
  creditStatus: string;
  status: string;
}

@Component({
  selector: 'app-cooperative-dashboard',
  templateUrl: './cooperative-dashboard.component.html',
  styleUrls: ['./cooperative-dashboard.component.scss']
})
export class CooperativeDashboardComponent implements OnInit {
  members: Member[] = [
    {
      id: 'M001',
      name: 'Kwame Osei',
      phone: '+237 84234597',
      region: 'North West',
      primaryCrop: 'Cocoa',
      farmSize: 5.2,
      lastProduction: 'Good',
      creditStatus: 'Active',
      status: 'Active'
    },
    {
      id: 'M002',
      name: 'Anna Boaleng',
      phone: '+237 78438887',
      region: 'South West',
      primaryCrop: 'Coffee',
      farmSize: 3.8,
      lastProduction: 'Good',
      creditStatus: 'Active',
      status: 'Active'
    },
    {
      id: 'M003',
      name: 'Yann Mensah',
      phone: '+237 84284756',
      region: 'Littoral',
      primaryCrop: 'Cocoa',
      farmSize: 7.5,
      lastProduction: 'Excellent',
      creditStatus: 'Paid',
      status: 'Inactive'
    },
    {
      id: 'M004',
      name: 'Alessia Datta',
      phone: '+237 55602833',
      region: 'North West',
      primaryCrop: 'Maize',
      farmSize: 4.2,
      lastProduction: 'Good',
      creditStatus: 'Active',
      status: 'Active'
    },
    {
      id: 'M005',
      name: 'Koffi Asante',
      phone: '+237 56700432',
      region: 'South West',
      primaryCrop: 'Cassava',
      farmSize: 6.0,
      lastProduction: 'Fair',
      creditStatus: 'Pending',
      status: 'Active'
    },
    {
      id: 'M006',
      name: 'Alessia Dumas',
      phone: '+237 88493844',
      region: 'Littoral',
      primaryCrop: 'Coffee',
      farmSize: 4.5,
      lastProduction: 'Excellent',
      creditStatus: 'Paid',
      status: 'Inactive'
    }
  ];

  ngOnInit(): void {}
}
