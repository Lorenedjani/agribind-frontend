import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductionListComponent } from './production-list/production-list.component';

@Component({
  selector: 'app-production',
  standalone: true,
  imports: [CommonModule, ProductionListComponent],
  template: `
    <div class="production-container">
      <app-production-list></app-production-list>
    </div>
  `,
  styles: [`
    .production-container {
      min-height: 100vh;
    }
  `]
})
export class ProductionComponent {}