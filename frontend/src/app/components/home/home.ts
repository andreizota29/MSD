import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {

  userName = '';
  role = '';

  constructor(private router: Router, private http: HttpClient) { }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  editProfile() {
    this.router.navigate(['/complete-profile'], { queryParams: { edit: 'true' } });
  }

  ngOnInit(){
      const token = localStorage.getItem('token');
      if(!token){
        return;
      }
      const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
      this.http.get<any>('http://localhost:5050/auth/me', {headers})
      .subscribe({
        next: (res) => {
          this.userName = `${res.firstName} ${res.lastName}`;
          this.role = res.role;
        },
        error: (err) => console.error('Error fetching user info:', err)
      });
  }



}
