import { Routes } from '@angular/router';
import { Auth } from './components/auth/auth';

export const routes: Routes = [
    { path: 'login', component: Auth },
    { path: '**', redirectTo: '' }
];