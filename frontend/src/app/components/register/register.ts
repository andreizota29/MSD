import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Alert } from '../../services/alert';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  errorMessage: string | null = null;

  register = new FormGroup({
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    phone: new FormControl('', [Validators.required]),
    email : new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)])
  });

  constructor(
      private httpClient: HttpClient,
      private router: Router,
      private alert: Alert
  ) {}

  handleSubmit() {
    this.errorMessage = null;
    if (this.register.invalid) {
        this.errorMessage = "Please fill all fields correctly.";
        return;
    }

    this.httpClient.post('http://localhost:5050/auth/register', this.register.value)
      .subscribe({
        next: () => {
          this.alert.success("Registration Successful! Please log in.");
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error(err);
          if (err.error && typeof err.error === 'object') {
             this.errorMessage = Object.values(err.error).join('\n');
          } else if (err.error && typeof err.error === 'string') {
             this.errorMessage = err.error;
          } else {
             this.errorMessage = "Registration failed.";
          }
        }
      });
  }
}