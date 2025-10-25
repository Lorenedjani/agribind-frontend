import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'cooperative',
    loadChildren: () => import('./modules/cooperative/cooperative.module').then(m => m.CooperativeModule)
  },
  { path: '', redirectTo: '/cooperative', pathMatch: 'full' }
];
