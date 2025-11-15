import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, RouterModule } from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {

  public data = new FormGroup({
    email : new FormControl('',[Validators.required]),
    password: new FormControl('',[Validators.required]),
    role: new FormControl('',[Validators.required])
  });

  constructor(private httpClient: HttpClient,
              private router: Router
  ) {}

public handleSubmit() {
  this.httpClient.post<{ token: string}>(
    'http://localhost:5050/auth/login',
    this.data.value
  ).subscribe({
    next: (res) => {
      localStorage.setItem('token', res.token);
      const payload = JSON.parse(atob(res.token.split('.')[1]));
      localStorage.setItem('role', payload.role);
      localStorage.setItem('email', payload.sub);

      if(payload.role === 'ADMIN'){
        this.router.navigate(['/admin-dashboard'])
      }
      else if(payload.role === 'DOCTOR'){
        this.router.navigate(['/doctor-dashboard']);
      }
      else if (payload.role === 'PATIENT' && !payload.profileCompleted) {
        this.router.navigate(['/complete-profile']);
      } 
      else {
        this.router.navigate(['/home']);
      }
    },
    error: (err) => {
      console.log(err);
      alert("Wrong credentials or role");
    }
  });
}

}
