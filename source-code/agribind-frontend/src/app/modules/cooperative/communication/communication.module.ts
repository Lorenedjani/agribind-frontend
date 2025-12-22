import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommunicationRoutingModule } from './communication-routing.module';
import { CommunicationDashboardComponent } from './communication-dashboard/communication-dashboard.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    CommunicationRoutingModule
  ]
})
export class CommunicationModule { }
