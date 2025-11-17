import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory-dashboard',
  standalone: true,
  imports: [CommonModule], // Remove CooperativeSidebarComponent from here
  templateUrl: './inventory-dashboard.component.html',
  styleUrls: ['./inventory-dashboard.component.scss']
})
export class InventoryDashboardComponent {
totalPages: any;
items: any;
Math: any;
getPageNumbers(): any {
throw new Error('Method not implemented.');
}
goToPage(arg0: number) {
throw new Error('Method not implemented.');
}
  totalValue = '425,000,000 XAF';
  farmerProducts = '287.5 MT';
  inputSupplies = '156 Items';
  criticalItems = 18;
currentPage: any;
}