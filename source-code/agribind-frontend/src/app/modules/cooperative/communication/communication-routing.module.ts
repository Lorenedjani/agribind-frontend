import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CommunicationDashboardComponent } from './communication-dashboard/communication-dashboard.component';

const routes: Routes = [
  {
    path: '',
    component: CommunicationDashboardComponent
  },
  {
    path: 'dashboard',
    redirectTo: '',
    pathMatch: 'full'
  }
];

@NgModule({
  declarations: [
    CommunicationDashboardComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class CommunicationRoutingModule { }