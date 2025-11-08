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
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  editProfile() {
  this.router.navigate(['/complete-profile'], { queryParams: { edit: 'true' } });
}
}
