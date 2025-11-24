import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./modules/auth/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: 'cooperative',
    loadComponent: () => import('./modules/cooperative/dashboard/cooperative-dashboard/cooperative-dashboard.component').then(c => c.CooperativeDashboardComponent),
    children: [
      {
        path: 'members',
        loadComponent: () => import('./modules/cooperative/members/member-list/member-list.component').then(c => c.MemberListComponent)
      },
      {
        path: 'production',
        loadComponent: () => import('./modules/cooperative/production/production.component').then(c => c.ProductionComponent)
      },
      {
        path: 'inventory',
        loadComponent: () => import('./modules/cooperative/inventory/inventory.component').then(c => c.InventoryComponent)
      },
      {
        path: '',
        redirectTo: 'members',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
