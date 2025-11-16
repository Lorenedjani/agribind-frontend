import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ProductionFormComponent } from '../production-form/production-form.component';
import { ProductionDetailComponent } from '../production-detail/production-detail.component';
import { DeleteProductionComponent } from '../delete-production/delete-production.component';

export interface Production {
  id: string;
  farmerId: string;
  farmerName: string;
  product: string;
  quantity: number;
  unit: string;
  quality: string;
  harvestDate: string;
  location: string;
  status: string;
  value: number;
}

@Component({
  selector: 'app-production-list',
  templateUrl: './production-list.component.html',
  styleUrls: ['./production-list.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ProductionFormComponent, ProductionDetailComponent, DeleteProductionComponent]
})
export class ProductionListComponent implements OnInit {
  @ViewChild(ProductionFormComponent, { static: false }) productionFormComponent!: ProductionFormComponent;
  @ViewChild(ProductionDetailComponent, { static: false }) productionDetailComponent!: ProductionDetailComponent;
  @ViewChild(DeleteProductionComponent, { static: false }) deleteProductionComponent!: DeleteProductionComponent;

  productions: Production[] = [
    {
      id: 'PR001',
      farmerId: 'M001',
      farmerName: 'Kwame Osei',
      product: 'Cocoa Beans',
      quantity: 500,
      unit: 'kg',
      quality: 'Grade A',
      harvestDate: '2025-11-10',
      location: 'North West',
      status: 'Verified',
      value: 1500000
    },
    {
      id: 'PR002',
      farmerId: 'M002',
      farmerName: 'Anna Boaleng',
      product: 'Coffee Beans',
      quantity: 300,
      unit: 'kg',
      quality: 'Grade A',
      harvestDate: '2025-11-12',
      location: 'South West',
      status: 'Pending',
      value: 900000
    },
    {
      id: 'PR003',
      farmerId: 'M003',
      farmerName: 'Yann Mensah',
      product: 'Cocoa Beans',
      quantity: 450,
      unit: 'kg',
      quality: 'Grade B',
      harvestDate: '2025-11-08',
      location: 'Littoral',
      status: 'Verified',
      value: 1200000
    },
    {
      id: 'PR004',
      farmerId: 'M001',
      farmerName: 'Kwame Osei',
      product: 'Palm Oil',
      quantity: 200,
      unit: 'L',
      quality: 'Premium',
      harvestDate: '2025-11-14',
      location: 'North West',
      status: 'Verified',
      value: 800000
    }
  ];

  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  ngOnInit(): void {
    this.calculatePagination();
  }

  calculatePagination(): void {
    this.totalPages = Math.ceil(this.productions.length / this.itemsPerPage);
  }

  get paginatedProductions(): Production[] {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return this.productions.slice(startIndex, endIndex);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  private getNextProductionId(): string {
    if (this.productions.length === 0) {
      return 'PR001';
    }

    const existingIds = this.productions.map(production => {
      const numericPart = production.id.replace('PR', '');
      return parseInt(numericPart, 10);
    });

    const maxId = Math.max(...existingIds);
    const nextId = maxId + 1;
    return 'PR' + nextId.toString().padStart(3, '0');
  }

  openAddProductionModal(): void {
    if (this.productionFormComponent) {
      this.productionFormComponent.openModal();
    } else {
      console.error('ProductionFormComponent not found!');
    }
  }

  openViewProductionModal(production: Production): void {
    if (this.productionDetailComponent) {
      this.productionDetailComponent.openModal(production);
    } else {
      console.error('ProductionDetailComponent not found!');
    }
  }

  openEditProductionModal(production: Production): void {
    if (this.productionFormComponent) {
      this.productionFormComponent.openModal(production);
    } else {
      console.error('ProductionFormComponent not found!');
    }
  }

  openDeleteConfirmModal(production: Production): void {
    if (this.deleteProductionComponent) {
      this.deleteProductionComponent.openModal(production);
    } else {
      console.error('DeleteProductionComponent not found!');
    }
  }

  onProductionAdded(newProduction: any): void {
    const nextId = this.getNextProductionId();
    const convertedProduction: Production = {
      id: nextId,
      farmerId: newProduction.farmerId,
      farmerName: newProduction.farmerName,
      product: newProduction.product,
      quantity: newProduction.quantity,
      unit: newProduction.unit,
      quality: newProduction.quality,
      harvestDate: newProduction.harvestDate,
      location: newProduction.location,
      status: newProduction.status || 'Pending',
      value: newProduction.value
    };

    this.productions.unshift(convertedProduction);
    this.calculatePagination();
    this.currentPage = 1;
  }

  onProductionUpdated(updatedProduction: Production): void {
    const index = this.productions.findIndex(production => production.id === updatedProduction.id);
    if (index !== -1) {
      this.productions[index] = updatedProduction;
    }
  }

  onProductionDeleted(deletedProduction: Production): void {
    const index = this.productions.findIndex(p => p.id === deletedProduction.id);
    if (index !== -1) {
      this.productions.splice(index, 1);
      this.calculatePagination();
      if (this.currentPage > this.totalPages && this.totalPages > 0) {
        this.currentPage = this.totalPages;
      }
    }
  }

  onModalClosed(): void {
    console.log('Modal closed');
  }

  getTotalProduction(): number {
    return this.productions.length;
  }

  getTotalQuantity(): number {
    return this.productions.reduce((sum, p) => sum + p.quantity, 0);
  }

  getTotalValue(): number {
    return this.productions.reduce((sum, p) => sum + p.value, 0);
  }

  getVerifiedCount(): number {
    return this.productions.filter(p => p.status === 'Verified').length;
  }

  formatCurrency(value: number): string {
    return value.toLocaleString('en-US') + ' XAF';
  }
}
