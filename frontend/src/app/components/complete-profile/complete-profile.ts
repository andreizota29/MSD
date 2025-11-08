import { CommonModule } from '@angular/common';
import { HttpClient, HttpHandler, HttpHeaderResponse, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-complete-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './complete-profile.html',
  styleUrl: './complete-profile.css',
})
export class CompleteProfile implements OnInit {

  bloodTypes = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];
  isEditMode = false;

  profileForm = new FormGroup({
    insuranceNumber: new FormControl('', Validators.required),
    dateOfBirth: new FormControl('', Validators.required),
    bloodType: new FormControl('', Validators.required),
    medicalHistory: new FormControl('')
  });

  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) { }

  ngOnInit() {
  const token = localStorage.getItem('token');
  if (!token) {
    this.router.navigate(['/login']);
    return;
  }

  this.route.queryParams.subscribe(params => {
    this.isEditMode = params['edit'] === 'true';

    const payload = JSON.parse(atob(token.split('.')[1]));

    if (payload.profileCompleted && !this.isEditMode) {
      this.router.navigate(['/home']);
      return;
    }

    if (this.isEditMode) {
      const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
      this.http.get<any>('http://localhost:5050/patient/me', { headers })
        .subscribe({
          next: (patient) => {
            this.profileForm.patchValue({
              insuranceNumber: patient.insuranceNumber,
              dateOfBirth: patient.dateOfBirth?.substring(0, 10),
              bloodType: patient.bloodType,
              medicalHistory: patient.medicalHistory
            });
          },
          error: (err) => console.error(err)
        });
    }
  });
}

  handleSubmit() {
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
        alert(this.isEditMode ? 'Profile updated successfully!' : 'Profile completed successfully!');
        this.router.navigate(['/home']);
      },
      error: (err) => console.log(err)
    });
  }

  goBack() {
    this.router.navigate(['/home']);
  }

  deleteAccount() {
    if(!confirm('Are you sure you want to delete your account?')){

    }
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({'Authorization': `Bearer ${token}`});

    this.http.delete('http://localhost:5050/patient/me', {headers})
    .subscribe({
      next: () => {
        alert('Account deleted successfully');
        localStorage.clear();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.log(err);
        alert('Failed to delete account');
      }
    });
  }

}
