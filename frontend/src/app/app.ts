import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { CustomAlert } from './components/custom-alert/custom-alert';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterModule, CustomAlert],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],
})
export class App {
  constructor(public router: Router) {}

  get isLoggedIn(): boolean {
    const url = this.router.url;
    return !(url.includes('/login') || url.includes('/register') || url.includes('/reset-password'));
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  changePass() {
    this.router.navigate(['/change-password']);
  }

}
