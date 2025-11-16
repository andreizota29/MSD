import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
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
    name: new FormControl('', Validators.required),
    price: new FormControl('', Validators.required)
  });

  addDoctorForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    phone: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
    title: new FormControl('', Validators.required),
    departmentId: new FormControl('', Validators.required),
    timetableTemplate: new FormControl('WEEKDAY_9_18', Validators.required)
  });

  editingDoctor: any = null;

  editDoctorForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    phone: new FormControl('', Validators.required),
    departmentId: new FormControl(''),
    timetableTemplate: new FormControl('', Validators.required),
    password: new FormControl('') 
  });

  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {
    this.loadData();
    this.addDoctorForm.valueChanges.subscribe(val => {
      console.log('Doctor form values changed:', val);
    });

    this.editDoctorForm.valueChanges.subscribe(val => {
      console.log('Edit doctor form values changed:', val);
    });
    const headers = this.getAuthHeaders();

    this.http.get<string[]>('http://localhost:5050/admin/timetable-templates', { headers })
      .subscribe({
        next: (res) => {
          this.timetableTemplates = res;
          if (!this.addDoctorForm.value.timetableTemplate) {
            this.addDoctorForm.patchValue({ timetableTemplate: res[0] });
          }
        },
        error: (err) => console.error(err)
      });
  }

  private updateFilteredServices() {
    if (this.selectedDepartmentId) {
      this.filteredServices = this.services.filter(
        s => s.department?.id === this.selectedDepartmentId
      );
    } else {
      this.filteredServices = [];
    }
  }



  onDepartmentChange() {
    const deptId = this.addServiceForm.value.departmentId;
    this.selectedDepartmentId = deptId ? +deptId : null;
    this.updateFilteredServices();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    console.log(token);
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }


  loadData() {
    const headers = this.getAuthHeaders();

    this.http.get<any[]>('http://localhost:5050/admin/departments', { headers })
      .subscribe({ next: res => this.departments = res, error: err => console.log(err) });

    this.http.get<any[]>('http://localhost:5050/admin/services', { headers })
      .subscribe({ next: res => { this.services = res; this.updateFilteredServices(); }, error: err => console.error(err) });

    this.http.get<any[]>('http://localhost:5050/admin/doctors', { headers })
      .subscribe({
        next: res => {
          console.log('Doctors from backend:', res);
          this.doctors = res;
        },
        error: err => console.error(err)
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
      .subscribe({
        next: () => {
          this.departments = this.departments.filter(d => d.id !== id);
          this.services = this.services.filter(s => s.department?.id !== id);

          if (this.selectedDepartmentId === id) {
            this.selectedDepartmentId = null;
            this.filteredServices = [];
          }
          this.http.get<any[]>('http://localhost:5050/admin/doctors', { headers })
            .subscribe({
              next: (res) => this.doctors = res,
              error: (err) => console.error(err)
            });
        },
        error: (err) => console.error(err)
      });
  }

  addService() {
    const headers = this.getAuthHeaders();

    const dept = this.departments.find(d => d.id == this.addServiceForm.value.departmentId);
    if (!dept) {
      alert('Department not found!');
      return;
    }

    const price = Number(this.addServiceForm.value.price);
    if (isNaN(price) || price <= 0) {
      alert('Price must be a positive number');
      return;
    }

    const payload = {
      name: this.addServiceForm.value.name ?? '',
      price: price,
      department: { id: dept.id }
    };

    this.http.post<any>('http://localhost:5050/admin/services', payload, { headers })
      .subscribe({
        next: (service) => {
          service.department = dept;
          this.services.push(service);
          this.updateFilteredServices();
          this.addServiceForm.reset();
        },
        error: (err) => console.error('Error adding service:', err)
      });
  }

  deleteService(id: number) {
    const headers = this.getAuthHeaders();
    this.http.delete(`http://localhost:5050/admin/services/${id}`, { headers })
      .subscribe(() => {
        this.services = this.services.filter(s => s.id !== id);
        this.updateFilteredServices();
      });
  }

  addDoctor() {
    const headers = this.getAuthHeaders();
    const deptId = Number(this.addDoctorForm.value.departmentId);
    const dept = this.departments.find(d => d.id === deptId);

    if (!dept) {
      console.error('Department not found for ID', deptId);
      alert('Select a valid department!');
      return;
    }

    if (!dept) {
      alert('Please select a valid department');
      return;
    }

    const payload = {
      user: {
        firstName: this.addDoctorForm.value.firstName?.trim(),
        lastName: this.addDoctorForm.value.lastName?.trim(),
        email: this.addDoctorForm.value.email?.trim(),
        phone: this.addDoctorForm.value.phone?.trim(),
        password: this.addDoctorForm.value.password?.trim()
      },
      title: this.addDoctorForm.value.title?.trim(),
      department: { id: Number(this.addDoctorForm.value.departmentId) },
      timetableTemplate: this.addDoctorForm.value.timetableTemplate
    };

    console.log('Payload to send:', payload);

    if (!payload.user.firstName || !payload.user.lastName || !payload.user.email ||
      !payload.user.password || !payload.title) {
      alert('Please fill all required fields');
      return;
    }
    console.log('Payload to send:', payload);
    this.http.post('http://localhost:5050/admin/doctors', payload, { headers })
      .subscribe({
        next: () => {
          alert('Doctor added successfully');
          this.addDoctorForm.reset();
          this.loadData();
        },
        error: (err) => console.error('Error adding doctor:', err)
      });
  }

  deleteDoctor(id: number) {
    const headers = this.getAuthHeaders();
    if (this.editingDoctor && this.editingDoctor.id === id) {
      this.closeEdit();
    }
    this.http.delete(`http://localhost:5050/admin/doctors/${id}`, { headers })
      .subscribe(() => this.doctors = this.doctors.filter(d => d.id !== id));
  }

  editDoctor(doc: any) {
    this.editingDoctor = doc;

    this.editDoctorForm.patchValue({
      firstName: doc.user.firstName,
      lastName: doc.user.lastName,
      phone: doc.user.phone,
      departmentId: doc.department?.id || '',
      timetableTemplate: doc.timetableTemplate,
      password: ''  
    });
  }

  updateDoctor() {
    if (!this.editingDoctor) return;

    const headers = this.getAuthHeaders();
    const id = this.editingDoctor.id;

    const payload = {
      user: {
        firstName: this.editDoctorForm.value.firstName,
        lastName: this.editDoctorForm.value.lastName,
        phone: this.editDoctorForm.value.phone,
        password: this.editDoctorForm.value.password || null 
      },
      department: this.editDoctorForm.value.departmentId
        ? { id: this.editDoctorForm.value.departmentId }
        : null,
      timetableTemplate: this.editDoctorForm.value.timetableTemplate
    };

    this.http.put(`http://localhost:5050/admin/doctors/${id}`, payload, { headers })
      .subscribe({
        next: () => {
          this.closeEdit();
          this.loadData();
        },
        error: (err) => console.error(err)
      });
  }

  closeEdit() {
    this.editingDoctor = null;
    this.editDoctorForm.reset();
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

}
