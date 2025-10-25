import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CooperativeDashboardComponent } from './dashboard/cooperative-dashboard/cooperative-dashboard.component';
import { CooperativeRoutingModule } from './cooperative-routing.module';

@NgModule({
  declarations: [

  ],
  imports: [
    CommonModule,
    CooperativeRoutingModule,
    CooperativeDashboardComponent
  ]
})
export class CooperativeModule { }
