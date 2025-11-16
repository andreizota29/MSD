import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, RouterModule } from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {

  showForgotPassword = false;

  public loginForm = new FormGroup({
    email : new FormControl('',[Validators.required]),
    password: new FormControl('',[Validators.required]),
    role: new FormControl('',[Validators.required])
  });

   public forgotForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  constructor(private httpClient: HttpClient,
              private router: Router
  ) {}

public handleLogin() {
    this.httpClient.post<{ token: string}>(
      'http://localhost:5050/auth/login',
      this.loginForm.value
    ).subscribe({
      next: (res) => {
        localStorage.setItem('token', res.token);
        const payload = JSON.parse(atob(res.token.split('.')[1]));
        localStorage.setItem('role', payload.role);
        localStorage.setItem('email', payload.sub);

        if(payload.role === 'ADMIN'){
          this.router.navigate(['/admin-dashboard'])
        }
        else if(payload.role === 'DOCTOR'){
          this.router.navigate(['/doctor-dashboard']);
        }
        else if (payload.role === 'PATIENT' && !payload.profileCompleted) {
          this.router.navigate(['/complete-profile']);
        } 
        else {
          this.router.navigate(['/patient-dashboard']);
        }
      },
      error: (err) => {
        console.log(err);
        alert("Wrong credentials or role");
      }
    });
  }

  public handleForgotPassword() {
  if (this.forgotForm.invalid) return;

  const email = this.forgotForm.value.email;
  console.log("Forgot password requested for:", email);

  this.httpClient.post('http://localhost:5050/auth/forgot-password', { email }, { responseType: 'text' })
  .subscribe({
    next: (res) => {
      console.log("Forgot password request sent successfully:", res);
      alert(`If this email exists in our system, a password reset link has been sent to ${email}`);
      this.showForgotPassword = false;
    },
    error: (err) => {
      console.error("Failed to send forgot password request", err);
      alert("Failed to send reset email. Try again later.");
    }
  });
}

  public toggleForgotPassword() {
    this.showForgotPassword = !this.showForgotPassword;
  }

}
