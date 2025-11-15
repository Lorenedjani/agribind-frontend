import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router'; // Import RouterModule
import { FormsModule } from '@angular/forms';

//import { MembersComponent } from './members/members.component';
import { ProductionComponent } from './production/production.component';
import { SharedModule } from '../../shared/shared.module';

// Define routes directly in the module
const routes: Routes = [
  //{ path: 'members', component: MembersComponent },
  { path: 'production', component: ProductionComponent },
  { path: '', redirectTo: 'members', pathMatch: 'full' }
];

@NgModule({
  declarations: [
   // MembersComponent,

  ],
  imports: [
    CommonModule,
    RouterModule.forChild(routes), // Add routing here
    FormsModule,
    SharedModule
  ]
})
export class CooperativeModule { }