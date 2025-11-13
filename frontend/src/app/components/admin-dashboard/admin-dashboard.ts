import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {

  departments: any[] = [];
  services: any[] = [];
  doctors: any[] = [];
  timetableTemplates: string[] = [];
  filteredServices: any[] = [];
  selectedDepartmentId: number | null = null;

  addDepartmentForm = new FormGroup({
    name: new FormControl('', Validators.required)
  });

  addServiceForm = new FormGroup({
    departmentId: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required)
  });

  addDoctorForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    phone: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
    title: new FormControl('', Validators.required),
    departmentId: new FormControl('', Validators.required),
    timetableTemplate: new FormControl('MON_FRI_9_18', Validators.required),
  });

  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {
    this.loadData();
    const headers = this.getAuthHeaders();
    this.http.get<string[]>('http://localhost:5050/admin/timetable-templates', { headers })
      .subscribe({
        next: (res) => this.timetableTemplates = res,
        error: (err) => console.error(err)
      });
  }

  onDepartmentChange() {
  if (this.selectedDepartmentId) {
    this.filteredServices = this.services.filter(
      s => s.department?.id == this.selectedDepartmentId
    );
  } else {
    this.filteredServices = [];
  }
}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    console.log(token);
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }


  loadData() {
    const headers = this.getAuthHeaders();

    this.http.get<any[]>('http://localhost:5050/admin/departments', { headers })
      .subscribe({
        next: (res) => this.departments = res,
        error: (err) => console.log(err)
      });

    this.http.get<any[]>('http://localhost:5050/admin/services', { headers })
      .subscribe({
        next: (res) => this.services = res,
        error: (err) => console.error(err)
      });

    this.http.get<any[]>('http://localhost:5050/admin/doctors', { headers })
      .subscribe({
        next: (res) => this.doctors = res,
        error: (err) => console.error(err)
      });
  }

  addDepartment() {
    const headers = this.getAuthHeaders();
    this.http.post('http://localhost:5050/admin/departments', this.addDepartmentForm.value, { headers })
      .subscribe({
        next: () => {
          this.addDepartmentForm.reset();
          this.loadData();
        },
        error: (err) => console.log(err)
      });
  }

  deleteDepartment(id: number) {
    const headers = this.getAuthHeaders();
    this.http.delete(`http://localhost:5050/admin/departments/${id}`, { headers })
      .subscribe(() => this.departments = this.departments.filter(d => d.id !== id));
  }

addService() {
  const headers = this.getAuthHeaders();
  const payload = {
    name: this.addServiceForm.value.name,
    department: { id: this.addServiceForm.value.departmentId }
  };

  const dept = this.departments.find(d => d.id == this.addServiceForm.value.departmentId);

  this.http.post<any>('http://localhost:5050/admin/services', payload, { headers })
    .subscribe({
      next: (service) => {
        service.department = dept;
        this.services.push(service);

        if (this.selectedDepartmentId === dept?.id) {
          this.filteredServices.push(service);
        }

        this.addServiceForm.reset();
      },
      error: (err) => console.error('Error adding service:', err)
    });
}

  deleteService(id: number) {
    const headers = this.getAuthHeaders();
    this.http.delete(`http://localhost:5050/admin/services/${id}`, { headers })
      .subscribe(() => this.services = this.services.filter(s => s.id !== id));
  }

  addDoctor() {
    const headers = this.getAuthHeaders();
    this.http.post('http://localhost:5050/admin/doctors', this.addDoctorForm.value, { headers })
      .subscribe({
        next: () => {
          alert('Doctor added successfully');
          this.addDoctorForm.reset();
          this.loadData();
        },
        error: (err) => console.error(err)
      });
  }

  deleteDoctor(id: number) {
    const headers = this.getAuthHeaders();
    this.http.delete(`http://localhost:5050/admin/doctors/${id}`, { headers })
      .subscribe(() => this.doctors = this.doctors.filter(d => d.id !== id));
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

}
