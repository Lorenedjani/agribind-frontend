import { Component } from '@angular/core';

@Component({
  selector: 'app-inventory-dashboard',
  templateUrl: './inventory-dashboard.component.html',
  styleUrls: ['./inventory-dashboard.component.scss']
})
export class InventoryDashboardComponent {
  totalValue = '425,000,000 XAF';
  farmerProducts = '287.5 MT';
  inputSupplies = '156 Items';
  criticalItems = 18;
}
