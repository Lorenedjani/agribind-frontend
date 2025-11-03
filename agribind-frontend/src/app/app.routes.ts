import { Routes } from '@angular/router';
import { MembersComponent } from './modules/cooperative/members/member.component';
import {MemberListComponent} from './modules/cooperative/members/member-list/member-list.component';

export const routes: Routes = [
  {
    path: 'cooperative',
    loadChildren: () => import('./modules/cooperative/cooperative.module').then(m => m.CooperativeModule)
  },
  { path: '', redirectTo: '/cooperative', pathMatch: 'full' },
  { path: '', redirectTo: '/members', pathMatch: 'full' },
  { path: 'members', component: MemberListComponent },
];
