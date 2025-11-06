import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  constructor(private router: Router){}

  logout() {
    localStorage.removeItem('token');

    localStorage.removeItem('role');
    localStorage.removeItem('email');

    this.router.navigate(['/login']);
  }
}
