import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CooperativeDashboardComponent } from './dashboard/cooperative-dashboard/cooperative-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: CooperativeDashboardComponent
    // Remove the children routes since the table is now in the dashboard
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CooperativeRoutingModule { }
