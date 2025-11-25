import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from "@angular/router";
import { Alert } from '../../services/alert';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  showForgotPassword = false;
  loginForm = new FormGroup({
    email : new FormControl('',[Validators.required]),
    password: new FormControl('',[Validators.required])
  });
  forgotForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  constructor(private httpClient: HttpClient, private router: Router, private alert: Alert) {}

  handleLogin() {
    this.httpClient.post<{ token: string}>(
      'http://localhost:5050/auth/login',
      this.loginForm.value
    ).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        const payload = JSON.parse(atob(res.token.split('.')[1]));
        localStorage.setItem('role', payload.role);
        localStorage.setItem('email', payload.sub);

        if(payload.role === 'ADMIN') this.router.navigate(['/admin-dashboard']);
        else if(payload.role === 'DOCTOR') this.router.navigate(['/doctor-dashboard']);
        else if (payload.role === 'PATIENT' && !payload.profileCompleted) this.router.navigate(['/complete-profile']);
        else this.router.navigate(['/patient-dashboard']);
      },
      error: (err) => {
        this.alert.error(err.error?.error || "Wrong credentials");
      }
    });
  }

  handleForgotPassword() {
    if (this.forgotForm.invalid) return;
    const email = this.forgotForm.value.email;
    this.httpClient.post('http://localhost:5050/auth/forgot-password', { email }, { responseType: 'text' })
    .subscribe({
      next: () => {
        this.alert.success(`Reset link sent to ${email} (if exists)`);
        this.showForgotPassword = false;
      },
      error: () => this.alert.error("Failed to send reset email.")
    });
  }

  toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
  }
}