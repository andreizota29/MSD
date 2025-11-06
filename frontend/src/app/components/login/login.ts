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
    role: new FormControl('PATIENT',[Validators.required])
  });

  constructor(private httpClient: HttpClient,
              private router: Router
  ) {}

public handleSubmit() {
  console.log(this.data.value); 
  this.httpClient.post<{ token: string }>(
    'http://localhost:5050/auth/login', 
    this.data.value
  ).subscribe({
    next: (res) => {
      const payLoad = JSON.parse(atob(res.token.split('.')[1]));
      localStorage.setItem('role', payLoad.role);
      localStorage.setItem('email', payLoad.sub);
      
      alert("Login successful");
      this.router.navigate(['/home']);
    },
    error: (err) => {
      console.log(err);
      alert("Wrong credentials or role");
    }
  });
}

}
