import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MemberListComponent } from './member-list/member-list.component';
import { MembersRoutingModule } from './members-routing.module';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    MembersRoutingModule,
    MemberListComponent,
  ]
})
export class MembersModule { }
