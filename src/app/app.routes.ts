import { Routes } from '@angular/router';
import { ProductionComponent } from './modules/cooperative/production/production.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'production',
    pathMatch: 'full'
  },
  {
    path: 'production',
    component: ProductionComponent
  },
  {
    path: 'test',
    component: ProductionComponent
  }
];
