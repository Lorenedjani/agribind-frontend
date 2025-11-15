import { Routes } from '@angular/router';
import { ProductionComponent } from './modules/cooperative/production/production.component';
import { MemberListComponent } from './modules/cooperative/members/member-list/member-list.component';

export const routes: Routes = [
  {
    path: 'production',
    component: ProductionComponent
  },
  {
    path: 'members',
    component: MemberListComponent
  },
  {
    path: '',
    redirectTo: '/production',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/production'
  }
];