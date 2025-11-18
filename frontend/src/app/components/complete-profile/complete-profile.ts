import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Alert } from '../../services/alert';

@Component({
  selector: 'app-complete-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './complete-profile.html',
  styleUrl: './complete-profile.css',
})
export class CompleteProfile implements OnInit {

  errorMessage: string | null = null;

  profileForm = new FormGroup({
    cnp: new FormControl('', Validators.required),
    dateOfBirth: new FormControl('', Validators.required),
  });

  constructor(private http: HttpClient, private router: Router, private alert: Alert) { }

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }
    
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.profileCompleted) {
             this.router.navigate(['/patient-dashboard']);
        }
    } catch(e) {
        this.router.navigate(['/login']);
    }
  }

  handleSubmit() {
    this.errorMessage = null;

    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.post<{ token: string }>(
      'http://localhost:5050/auth/complete-profile',
      this.profileForm.value,
      { headers }
    ).subscribe({
      next: (res) => {
        if (res.token) {
          localStorage.setItem('token', res.token);
        }
        this.alert.success('Profile completed successfully!');
        this.router.navigate(['/patient-dashboard']);
      },
      error: (err) => {
        console.error(err);
        if (err.error && err.error.error) {
          this.errorMessage = err.error.error; 
        } else {
          this.errorMessage = "Failed to save profile. Check your data.";
        }
      }
    });
  }
}