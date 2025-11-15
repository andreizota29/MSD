import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './doctor-dashboard.html',
  styleUrl: './doctor-dashboard.css',
})
export class DoctorDashboard implements OnInit {

  appointments: any[] = [];

  constructor(private http: HttpClient, private router: Router) {
  }


  ngOnInit(){
      this.loadAppointments();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({'Authorization': `Bearer ${token}`});
  }


  loadAppointments(){
    const headers = this.getAuthHeaders();
    this.http.get<any[]>('http://localhost:5050/doctor/appointments', {headers})
    .subscribe({
      next: (res) => this.appointments = res,
      error: (err) => console.log('Error loading appointments', err)
    });
  }

  logout(){
    localStorage.clear();
    this.router.navigate(['/login']);
  }

}
