import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class Auth {
  showRegister = false;

  userType: 'patient' | 'staff' = 'patient';

  email = '';
  password = '';

  firstName = '';
  lastName = '';
  phone = '';

  passwordVisible = false;

  toggleForm() {
    this.showRegister = !this.showRegister;
    this.clearFields();
  }

  togglePasswordVisibility() {
    this.passwordVisible = !this.passwordVisible;
  }

  setUserType(type: 'patient' | 'staff') {
    this.userType = type;
  }

  onLoginSubmit() {
    console.log('Login:', { userType: this.userType, email: this.email, password: this.password });
  }

  onRegisterSubmit() {
    console.log('Register:', { firstName: this.firstName, lastName: this.lastName, email: this.email, phone: this.phone, password: this.password });
  }

  clearFields() {
    this.email = '';
    this.password = '';
    this.firstName = '';
    this.lastName = '';
    this.phone = '';
  }
}
