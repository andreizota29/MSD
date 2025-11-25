import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Alert } from '../../services/alert';


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
  editingDoctor: any = null;

  addDepartmentForm = new FormGroup({
    name: new FormControl('', Validators.required)
  });

  addServiceForm = new FormGroup({
    departmentId: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required),
    price: new FormControl('', [Validators.required, Validators.min(0)])
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

  editDoctorForm = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    phone: new FormControl('', Validators.required),
    title: new FormControl('', Validators.required),
    departmentId: new FormControl('', Validators.required),
    timetableTemplate: new FormControl('', Validators.required),
    password: new FormControl('')
  });

  constructor(private http: HttpClient, private router: Router, private alert: Alert) { }

  ngOnInit() {
    this.loadData();
    this.loadTemplates();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  loadTemplates() {
    this.http.get<string[]>('http://localhost:5050/admin/timetable-templates', { headers: this.getAuthHeaders() })
      .subscribe({
        next: (res) => {
          this.timetableTemplates = res;
          if (!this.addDoctorForm.value.timetableTemplate && res.length > 0) {
            this.addDoctorForm.patchValue({ timetableTemplate: res[0] });
          }
        }
      });
  }

  loadData() {
    const headers = this.getAuthHeaders();

    this.http.get<any[]>('http://localhost:5050/admin/departments', { headers })
      .subscribe({ next: res => this.departments = res });

    this.http.get<any[]>('http://localhost:5050/admin/services', { headers })
      .subscribe({ 
        next: res => { 
          this.services = res; 
          this.updateFilteredServices(); 
        }
      });

    this.http.get<any[]>('http://localhost:5050/admin/doctors', { headers })
      .subscribe({ next: res => this.doctors = res });
  }

  addDepartment() {
    this.http.post('http://localhost:5050/admin/departments', this.addDepartmentForm.value, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.addDepartmentForm.reset();
          this.loadData();
          this.alert.success('Department added');
        },
        error: (err) => this.handleError(err)
      });
  }

  deleteDepartment(id: number) {
    if(!confirm('Delete department?')) return;
    this.http.delete(`http://localhost:5050/admin/departments/${id}`, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.loadData();
          this.alert.success('Department deleted');
        },
        error: (err) => this.handleError(err)
      });
  }

  addService() {
    const deptId = this.addServiceForm.value.departmentId;
    const dept = this.departments.find(d => d.id == deptId);
    
    if (!dept) return this.alert.error('Invalid Department');

    const payload = {
      name: this.addServiceForm.value.name,
      price: this.addServiceForm.value.price,
      department: { id: dept.id }
    };

    this.http.post('http://localhost:5050/admin/services', payload, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.addServiceForm.reset();
          this.loadData();
          this.alert.success('Service added');
        },
        error: (err) => this.handleError(err)
      });
  }

  deleteService(id: number) {
    if(!confirm('Delete service?')) return;
    this.http.delete(`http://localhost:5050/admin/services/${id}`, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.services = this.services.filter(s => s.id !== id);
          this.updateFilteredServices();
          this.alert.success('Service deleted');
        },
        error: (err) => this.handleError(err)
      });
  }

  addDoctor() {
    const deptId = Number(this.addDoctorForm.value.departmentId);
    if (!deptId) return this.alert.error('Select a department');

    const payload = {
      user: {
        firstName: this.addDoctorForm.value.firstName?.trim(),
        lastName: this.addDoctorForm.value.lastName?.trim(),
        email: this.addDoctorForm.value.email?.trim(),
        phone: this.addDoctorForm.value.phone?.trim(),
        password: this.addDoctorForm.value.password?.trim()
      },
      title: this.addDoctorForm.value.title?.trim(),
      department: { id: deptId },
      timetableTemplate: this.addDoctorForm.value.timetableTemplate
    };

    this.http.post('http://localhost:5050/admin/doctors', payload, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.alert.success('Doctor created');
          this.addDoctorForm.reset();
          this.loadData();
          this.loadTemplates(); 
        },
        error: (err) => this.handleError(err)
      });
  }

  updateDoctor() {
    if (!this.editingDoctor) return;
    const id = this.editingDoctor.id;

    const payload = {
      user: {
        firstName: this.editDoctorForm.value.firstName,
        lastName: this.editDoctorForm.value.lastName,
        email: this.editDoctorForm.value.email,
        phone: this.editDoctorForm.value.phone,
        password: this.editDoctorForm.value.password || null
      },
      title: this.editDoctorForm.value.title,
      department: this.editDoctorForm.value.departmentId ? { id: this.editDoctorForm.value.departmentId } : null,
      timetableTemplate: this.editDoctorForm.value.timetableTemplate
    };

    this.http.put(`http://localhost:5050/admin/doctors/${id}`, payload, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.alert.success('Doctor updated');
          this.closeEdit();
          this.loadData();
        },
        error: (err) => this.handleError(err)
      });
  }

  deleteDoctor(id: number) {
    if(!confirm('Delete doctor? This cannot be undone.')) return;
    this.http.delete(`http://localhost:5050/admin/doctors/${id}`, { headers: this.getAuthHeaders() })
      .subscribe({
        next: () => {
          this.doctors = this.doctors.filter(d => d.id !== id);
          this.alert.success('Doctor deleted');
        },
        error: (err) => this.handleError(err)
      });
  }

  editDoctor(doc: any) {
    this.editingDoctor = doc;
    this.editDoctorForm.patchValue({
      firstName: doc.user.firstName,
      lastName: doc.user.lastName,
      email: doc.user.email,
      phone: doc.user.phone,
      title: doc.title,
      departmentId: doc.department?.id || '',
      timetableTemplate: doc.timetableTemplate,
      password: ''
    });
  }

  closeEdit() {
    this.editingDoctor = null;
    this.editDoctorForm.reset();
  }

  onDepartmentChange() {
    const deptId = this.addServiceForm.value.departmentId;
    this.selectedDepartmentId = deptId ? +deptId : null;
    this.updateFilteredServices();
  }

  private updateFilteredServices() {
    if (this.selectedDepartmentId) {
      this.filteredServices = this.services.filter(s => s.department?.id === this.selectedDepartmentId);
    } else {
      this.filteredServices = [];
    }
  }

  private handleError(err: any) {
    console.error(err);
    let msg = 'Operation failed';
    if (err.error) {
      if (typeof err.error === 'object') {
        if(err.error.error) {
            msg = err.error.error; 
        } else {
            msg = Object.values(err.error).join('\n'); 
        }
      } else if (typeof err.error === 'string') {
        msg = err.error;
      }
    }
    this.alert.error(msg);
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}