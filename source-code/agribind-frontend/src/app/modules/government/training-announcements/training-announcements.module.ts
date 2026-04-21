import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { TrainingAnnouncementsComponent } from './training-announcements.component';

/**
 * TrainingAnnouncementsModule
 *
 * Self-contained feature module for the Government Training Announcements page.
 *
 * Usage:
 *  1. Import this module in your AppModule (or routing feature module):
 *       imports: [TrainingAnnouncementsModule]
 *
 *  2. Use the selector in any template:
 *       <app-training-announcements></app-training-announcements>
 *
 * Dependencies:
 *  - CommonModule      → *ngFor, *ngIf, async pipe, number pipe, etc.
 *  - FormsModule       → [(ngModel)] two-way data binding
 *  - HttpClientModule  → HTTP calls to /api/communications/government/*
 *
 * NOTE: If HttpClientModule is already imported at the root AppModule level
 * you can safely remove it from this module's imports array — Angular shares
 * the singleton HttpClient across the whole app.
 */
@NgModule({
  declarations: [
    TrainingAnnouncementsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
  ],
  exports: [
    TrainingAnnouncementsComponent
  ]
})
export class TrainingAnnouncementsModule {}
