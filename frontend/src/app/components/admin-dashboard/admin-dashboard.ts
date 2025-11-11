import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit{

  users: any[] = [];
  clinics: any[] = [];

  constructor(private http: HttpClient, private router: Router){}

  ngOnInit() {

    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({'Authorization': `Bearer ${token}`});
    const loggedInUserId = this.getUserIdFromToken(token);
    this.http.get<any[]>('http://localhost:5050/admin/users', { headers })
      .subscribe(users => {
        this.users = users.filter(u => u.userId !== loggedInUserId);
      });

    this.http.get<any[]>('http://localhost:5050/admin/clinics', { headers})
    .subscribe(clinics => this.clinics = clinics);
  }

  deleteUser(id: number){
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({'Authorization': `Bearer ${token}`});

    this.http.delete(`http://localhost:5050/admin/users/${id}`, {headers })
    .subscribe(() => this.users = this.users.filter(u => u.userId !== id));
  }

  deleteClinic(id: number){
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({'Authorization': `Bearer ${token}`});

    this.http.delete(`http://localhost:5050/admin/clinics/${id}`, {headers })
    .subscribe(() => this.clinics = this.clinics.filter(c => c.clinicId !== id));
  }

  private getUserIdFromToken(token: string | null): number | null {
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId || null;
    } catch (e) {
      console.error('Invalid token', e);
      return null;
    }
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

}
