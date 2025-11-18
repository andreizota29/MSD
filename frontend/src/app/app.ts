import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { CustomAlert } from './components/custom-alert/custom-alert';
import { Alert } from './services/alert';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterModule, CustomAlert],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  constructor(public router: Router, private alert: Alert, private http: HttpClient) {}

  get isLoggedIn(): boolean {
    const url = this.router.url;
    return !(url.includes('/login') || url.includes('/register') || url.includes('/reset-password'));
  }

  get isPatient(): boolean {
    return localStorage.getItem('role') === 'PATIENT';
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  changePass() {
    this.router.navigate(['/change-password']);
  }

  deleteAccount() {
    this.alert.confirm('This will permanently delete your account and all appointment history. This action cannot be undone.', () => {
      const token = localStorage.getItem('token');
      const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

      this.http.delete('http://localhost:5050/patient/me', { headers })
        .subscribe({
          next: () => {
            this.alert.success('Account deleted successfully.');
            this.logout(); 
          },
          error: (err) => {
            console.error(err);
            this.alert.error('Failed to delete account.');
          }
        });
    });
  }

}
