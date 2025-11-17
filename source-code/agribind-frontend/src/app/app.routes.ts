import { Routes } from '@angular/router';
import { ProductionComponent } from './modules/cooperative/production/production.component';
import { MemberListComponent } from './modules/cooperative/members/member-list/member-list.component';
import { InventoryComponent } from './modules/cooperative/inventory/inventory.component';
import { MembersComponent } from './modules/cooperative/members/member.component';

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
    path: 'inventory',
    component: InventoryComponent
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