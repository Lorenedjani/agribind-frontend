import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { InventoryItem } from '../../../models/item.model';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  constructor() {}

  // Return mock items grouped by category type (farmer/input/livestock can be filtered in the component)
  getItems(): Observable<InventoryItem[]> {
    const items: InventoryItem[] = [
      {
        id: 'PRD001', name: 'Cocoa Beans (Grade A)', category: 'Cocoa', quantityLabel: '168.3 MT', quantityValue: 168.3, minLabel: 'Min: 50 MT', valueXaf: '353,430,000 XAF', location: 'Douala Warehouse', status: 'In Stock', trend: 'up',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      {
        id: 'PRD002', name: 'Coffee Beans (Arabica A)', category: 'Coffee', quantityLabel: '82.5 MT', quantityValue: 82.5, minLabel: 'Min: 30 MT', valueXaf: '181,500,000 XAF', location: "Yaoundé Warehouse", status: 'In Stock', trend: 'up',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      {
        id: 'PRD003', name: 'Palm Oil (Processed)', category: 'Palm Oil', quantityLabel: '15.2 MT', quantityValue: 15.2, minLabel: 'Min: 25 MT', valueXaf: '24,320,000 XAF', location: 'Douala Warehouse', status: 'Low Stock', trend: 'down',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      // input supplies
      {
        id: 'INP001', name: 'Cocoa Seedlings (Hybrid)', category: 'Seeds', quantityLabel: '5,000 units', quantityValue: 5000, minLabel: 'Min: 2,000 units', valueXaf: '2,500,000 XAF', location: 'Central Depot', status: 'In Stock', trend: 'flat',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      {
        id: 'INP003', name: 'Organic Compost', category: 'Fertilizers', quantityLabel: '8.5 MT', quantityValue: 8.5, minLabel: 'Min: 15 MT', valueXaf: '1,700,000 XAF', location: 'Yaoundé Depot', status: 'Low Stock', trend: 'down',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      // livestock
      {
        id: 'LIV001', name: 'Cattle (Adult)', category: 'Cattle', quantityLabel: '125 heads', quantityValue: 125, minLabel: 'Min: 50 heads', valueXaf: '187,500,000 XAF', location: 'Ranch A - Adamawa', status: 'In Stock', trend: 'up',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
      {
        id: 'LIV004', name: 'Pigs (Breeding)', category: 'Pigs', quantityLabel: '42 heads', quantityValue: 42, minLabel: 'Min: 60 heads', valueXaf: '21,000,000 XAF', location: 'Farm D - Centre', status: 'Low Stock', trend: 'down',
        lastUpdated: '',
        item: undefined,
        supplier: undefined,
        quantity: 0
      },
    ];

    return of(items);
  }
}
