import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const payload = JSON.parse(atob(token.split('.')[1]));
  if (payload.role === 'PATIENT' && !payload.profileCompleted) {
    router.navigate(['/complete-profile']);
    return false;
  }

  const allowedRoles = route.data['roles'] as string[] | undefined;
  if (allowedRoles && !allowedRoles.includes(payload.role)) {
    if (payload.role === 'ADMIN') router.navigate(['/admin-dashboard']);
    else if (payload.role === 'DOCTOR') router.navigate(['/doctor-dashboard']);
    else router.navigate(['/patient-dashboard']); 
    return false;
  }

  return true;
};