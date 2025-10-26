import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MemberListComponent } from './member-list/member-list.component';
import { MembersRoutingModule } from './members-routing.module';

@NgModule({
  declarations: [], // Empty declarations
  imports: [
    CommonModule,
    MembersRoutingModule,
    MemberListComponent // Import standalone component
  ]
})
export class MembersModule { }
