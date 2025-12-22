// E:\INGE 4 ISI\Tutorial Project\agribind-platform\source-code\agribind-frontend\src\app\app.routes.ts

import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Public routes
  {
    path: 'login',
    loadComponent: () => import('./modules/auth/login/login.component').then(c => c.LoginComponent)
  },
  {
    path: 'change-password',
    loadComponent: () => import('./modules/auth/change-password/change-password.component').then(c => c.ChangePasswordComponent),
    canActivate: [authGuard]
  },

  // Cooperative routes (protected)
  {
    path: 'cooperative',
    loadComponent: () => import('./modules/cooperative/dashboard/cooperative-dashboard/cooperative-dashboard.component').then(c => c.CooperativeDashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['COOPERATIVE'] },
    children: [
      {
        path: '',
        redirectTo: 'members',
        pathMatch: 'full'
      },
      {
        path: 'members',
        loadComponent: () => import('./modules/cooperative/members/member-list/member-list.component').then(c => c.MemberListComponent)
      },
      {
        path: 'production',
        loadComponent: () => import('./modules/cooperative/production/production.component').then(c => c.ProductionComponent)
      },
      {
        path: 'communication',
        loadComponent: () => import('./modules/cooperative/communication/communication-wrapper.component').then(c => c.CommunicationWrapperComponent)
      },
      {
        path: 'inventory',
        loadComponent: () => import('./modules/cooperative/inventory/inventory.component').then(c => c.InventoryComponent)
      }
    ]
  },

  // Farmer routes (protected)
  /* {
    path: 'farmer',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/farmer/dashboard/farmer-dashboard.component').then(c => c.FarmerDashboardComponent)
      }
    ]
  },
 */
  // Government routes (protected)
  {
    path: 'government',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['GOVERNMENT'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/government/dashboard/government-dashboard/government-dashboard.component').then(c => c.GovernmentDashboardComponent)
      }
    ]
  },

  // Unauthorized route
  {
    path: 'unauthorized',
    redirectTo: '/login',
    pathMatch: 'full'
  },

  // Default routes
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