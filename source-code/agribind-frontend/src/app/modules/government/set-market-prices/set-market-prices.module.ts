import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { SetMarketPricesComponent } from './set-market-prices.component';

// FIX: SetMarketPricesComponent is standalone: true.
// Standalone components must NOT be listed in `declarations` — only in `imports`.
// This module now only sets up the lazy-loaded route.
const routes: Routes = [
  { path: '', component: SetMarketPricesComponent }
];

@NgModule({
  imports: [
    SetMarketPricesComponent,           // ← import (not declare) standalone component
    RouterModule.forChild(routes)
  ]
})
export class SetMarketPriceModule {}
