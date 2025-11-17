import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CooperativeDashboardComponent } from './dashboard/cooperative-dashboard/cooperative-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: CooperativeDashboardComponent,
    children: [
      {
        path: 'members',
        loadChildren: () => import('./members/members.module').then(m => m.MembersModule)
      },
      { path: '', redirectTo: 'members', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CooperativeRoutingModule { }
