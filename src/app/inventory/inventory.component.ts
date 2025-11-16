import { Component, OnInit } from '@angular/core';
import { InventoryService } from './inventory.service';
import { InventoryItem } from '../models/item.model';
import { InventoryDashboardComponent } from "./inventory-dashboard/inventory-dashboard.component";

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss'],
  imports: [InventoryDashboardComponent]
})
export class InventoryComponent implements OnInit {
  items: InventoryItem[] = [];
  activeTab: 'farmer'|'input'|'livestock' = 'farmer';

  constructor(private svc: InventoryService) {}

  ngOnInit(): void {
    this.svc.getItems().subscribe(i => this.items = i);
  }

  switchTab(tab: 'farmer'|'input'|'livestock') { this.activeTab = tab; }

  filteredItems() {
    if (this.activeTab === 'farmer') return this.items.filter(it => ['Cocoa','Coffee','Palm Oil','Cotton','Cassava','Maize'].includes(it.category));
    if (this.activeTab === 'input') return this.items.filter(it => ['Seeds','Fertilizers','Equipment','Livestock Feed'].includes(it.category));
    return this.items.filter(it => ['Cattle','Goats','Poultry','Pigs','Sheep'].includes(it.category));
  }
}
