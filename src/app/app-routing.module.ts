// In app-routing.module.ts - FIXED VERSION
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'cooperative',
    loadChildren: () => import('./modules/cooperative/cooperative.module').then(m => m.CooperativeModule)
  },
  {
    path: 'inventory',
    loadChildren: () => import('./modules/cooperative/inventory/inventory.module').then(m => m.InventoryModule)
  },
  {
    path: '',
    redirectTo: '/cooperative',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/cooperative'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }