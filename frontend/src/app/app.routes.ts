import { Routes } from '@angular/router';
import { Register } from './components/register/register';
import { Login } from './components/login/login';
import { Home } from './components/home/home';
import { authGuard } from './guards/auth-guard';
import { noAuthGuard } from './guards/no-auth-guard';
import { CompleteProfile } from './components/complete-profile/complete-profile';
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard';
import { DoctorDashboard } from './components/doctor-dashboard/doctor-dashboard';


export const routes: Routes = [
  { path: 'complete-profile', component: CompleteProfile,  data: { roles: ['PATIENT'] }  },
  { path: 'doctor-dashboard', component: DoctorDashboard, canActivate: [authGuard],  data: { roles: ['DOCTOR'] } },
  { path: 'admin-dashboard', component: AdminDashboard, canActivate: [authGuard],  data: { roles: ['ADMIN'] } },
  { path: 'home', component: Home, canActivate: [authGuard] ,  data: { roles: ['PATIENT'] } },
  { path: 'login', component: Login, canActivate: [noAuthGuard] },
  { path: 'register', component: Register, canActivate: [noAuthGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];