// src/app/core/guards/role.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const expectedRoles = route.data['roles'] as string[];
  const userRole = authService.getUserRole();

  if (!userRole) {
    console.log('❌ No user role found, redirecting to login');
    router.navigate(['/login']);
    return false;
  }

  if (expectedRoles && expectedRoles.includes(userRole.toUpperCase())) {
    console.log('✅ User has required role:', userRole);
    return true;
  }

  console.log('❌ User does not have required role. Expected:', expectedRoles, 'Got:', userRole);
  router.navigate(['/unauthorized']);
  return false;
};
