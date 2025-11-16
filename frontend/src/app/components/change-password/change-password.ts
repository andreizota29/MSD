import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

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

  constructor(private http: HttpClient, private router : Router){
    
  }

  getAuthHeaders(){
    const token = localStorage.getItem('token');
    return { headers : new HttpHeaders({ Authorization: `Bearer ${token}`})};
  }

  changePassword() {
    if(this.newPassword !== this.confirmPassword){
      alert('Password do not match');
      return;
    }
    if(this.newPassword.length < 8) {
      alert('Password must be at least 6 characters long');
      return;
    }
    this.http.put('http://localhost:5050/auth/change-password', {newPassword: this.newPassword}, this.getAuthHeaders())
    .subscribe({
      next: () => {
        alert('Password changed successfully!');
        this.router.navigate(['/login']);
      },
      error: err => {
        console.log(err);
        alert('Error changing password');
      }
    });
  }

}
