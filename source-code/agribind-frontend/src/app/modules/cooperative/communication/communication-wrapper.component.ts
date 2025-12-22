import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CommunicationDashboardComponent } from './communication-dashboard/communication-dashboard.component';

@Component({
  selector: 'app-communication-wrapper',
  standalone: true,
  imports: [CommonModule, CommunicationDashboardComponent],
  template: `<app-communication-dashboard></app-communication-dashboard>`
})
export class CommunicationWrapperComponent {}
