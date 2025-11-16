import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {

  public form = new FormGroup({
    newPassword: new FormControl('', [Validators.required]),
    confirmPassword: new FormControl('', [Validators.required])
  });

  private token: string | null = null;

  constructor(private route: ActivatedRoute, private http: HttpClient, private router: Router) {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }
  public handleReset() {
    if (this.form.invalid) return;
    if (this.form.value.newPassword !== this.form.value.confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    this.http.post('http://localhost:5050/auth/reset-password', {
      token: this.token,
      newPassword: this.form.value.newPassword
    }).subscribe({
      next: () => {
        alert("Password changed successfully");
        localStorage.clear();
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error(err);
        alert("Failed to reset password");
      }
    });
  }
}