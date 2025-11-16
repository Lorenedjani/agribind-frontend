import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
<<<<<<< HEAD
  {
    path: 'cooperative',
    loadChildren: () => import('./cooperative/cooperative.module').then(m => m.CooperativeModule)
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
=======
  { path: 'inventory', loadChildren: () => import('./inventory/inventory.module').then(m => m.InventoryModule) },
  { path: '', redirectTo: '/inventory', pathMatch: 'full' },
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
<<<<<<< HEAD
export class AppRoutingModule { }
=======
export class AppRoutingModule {}
>>>>>>> 8727ae50342ccc2da80d720dd9ae9fe49958e3f8
