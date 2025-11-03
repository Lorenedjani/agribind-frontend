import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'cooperative',
    loadChildren: () => import('./cooperative/cooperative.module').then(m => m.CooperativeModule)
  },
  {
    path: '',
    redirectTo: '/cooperative',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: '/cooperative'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
