import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryComponent } from './inventory.component';
import { InventoryDashboardComponent } from './inventory-dashboard/inventory-dashboard.component';
import { InventoryListComponent } from './inventory-list/inventory-list.component';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@NgModule({
  declarations: [InventoryComponent, InventoryDashboardComponent, InventoryListComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild([{ path: '', component: InventoryComponent }])],
})
export class InventoryModule {}
