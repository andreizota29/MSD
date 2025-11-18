import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Alert } from '../../services/alert';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css',
})
export class ChangePassword {
  newPassword = '';
  confirmPassword = '';

  constructor(private http: HttpClient, public router: Router, private alert: Alert) { }

  getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  changePassword() {
    if (this.newPassword !== this.confirmPassword) {
      this.alert.error('Passwords do not match');
      return;
    }
    if (this.newPassword.length < 8) {
      this.alert.error('Password must be at least 8 characters long');
      return;
    }
    
    this.http.put('http://localhost:5050/auth/change-password', 
      { newPassword: this.newPassword }, 
      this.getAuthHeaders()
    ).subscribe({
      next: () => {
        this.alert.success('Password changed successfully!');
        this.router.navigate(['/login']);
      },
      error: err => {
        console.log(err);
        this.alert.error('Error changing password');
      }
    });
  }
}