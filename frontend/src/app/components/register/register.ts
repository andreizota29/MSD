import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  public register = new FormGroup({
    firstName: new FormControl('',[Validators.required]),
    lastName: new FormControl('',[Validators.required]),
    phone: new FormControl('',[Validators.required]),
    email : new FormControl('',[Validators.required]),
    password: new FormControl('',[Validators.required]),
    role: new FormControl('PATIENT')
  });

  constructor(private httpClient: HttpClient,
              private router: Router
  ) {}

  public handleSubmit() {
    console.log(this.register.value); 
    this.httpClient.post('http://localhost:5050/auth/register', this.register.value).subscribe(data => {
      alert("Registation Successfully");
      this.router.navigate(['/login']);
    }, error => {
      console.log(error);
    })
  }
}
