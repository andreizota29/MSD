import { Routes } from '@angular/router';
import { Register } from './components/register/register';
import { Login } from './components/login/login';
import { Home } from './components/home/home';
import { authGuard } from './guards/auth-guard';
import { noAuthGuard } from './guards/no-auth-guard';


export const routes: Routes = [
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'login', component: Login, canActivate: [noAuthGuard] },
  { path: 'register', component: Register, canActivate: [noAuthGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];