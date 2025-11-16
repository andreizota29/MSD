import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Department {
  id: number;
  name: string;
}

interface ClinicService {
  id: number;
  name: string;
  price: number;
}

interface Doctor {
  id: number;
  user: {
    firstName: string;
    lastName: string;
  };
}

interface Slot {
  id: number;
  doctor: Doctor;
  date: string;
  startTime: string;
  endTime: string;
  booked: boolean;
}

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patient-dashboard.html',
  styleUrls: ['./patient-dashboard.css'],
})
export class PatientDashboard implements OnInit {

  userName = '';
  role = '';

  departments: Department[] = [];
  services: ClinicService[] = [];
  selectedDepartmentId: number | null = null;
  selectedServiceId: number | null = null;
  groupedSlots: { doctor: Doctor; slots: Slot[] }[] = [];

  selectedDate: Date = new Date();
  slots: Slot[] = [];
  selectedSlotId: number | null = null;

  constructor(private router: Router, private http: HttpClient) { }

  ngOnInit() {
    this.loadUser();
    this.loadDepartments();

  }
  get formattedDate(): string {
    return this.selectedDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' });
  }

  getAuthHeaders() {
    const token = localStorage.getItem('token');
    console.log(token);
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  loadUser() {
    this.http.get<any>('http://localhost:5050/auth/me', this.getAuthHeaders())
      .subscribe(res => {
        this.userName = `${res.firstName} ${res.lastName}`;
        this.role = res.role;
      });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  editProfile() {
    this.router.navigate(['/complete-profile'], { queryParams: { edit: 'true' } });
  }

  loadDepartments() {
    this.http.get<Department[]>('http://localhost:5050/patient/departments', this.getAuthHeaders())
      .subscribe(res => this.departments = res);
  }

  onDepartmentChange() {
    this.selectedServiceId = null;
    this.services = [];
    if (!this.selectedDepartmentId) return;

    this.http.get<ClinicService[]>(`http://localhost:5050/patient/departments/${this.selectedDepartmentId}/services`, this.getAuthHeaders())
      .subscribe(res => this.services = res);
  }

  prevDay() {
    const today = new Date();
    today.setHours(0, 0, 0, 0); 

    const newDate = new Date(this.selectedDate);
    newDate.setDate(newDate.getDate() - 1);

    if (newDate < today) return; 

    this.selectedDate = newDate;
    this.loadSlots();
  }

  nextDay() {
    this.selectedDate.setDate(this.selectedDate.getDate() + 1);
    this.loadSlots();
  }

  loadSlots() {
    if (!this.selectedDepartmentId || !this.selectedServiceId) return;

    const dateStr = this.selectedDate.toISOString().split('T')[0];
    this.http.get<Slot[]>(`http://localhost:5050/patient/departments/${this.selectedDepartmentId}/services/${this.selectedServiceId}/slots?date=${dateStr}`, this.getAuthHeaders())
      .subscribe(res => {
        const freeSlots = res.filter(s => !s.booked);

        freeSlots.forEach((slot, index) => {
          if (!slot.id) {
            slot.id = index + 1;
          }
        });

        const groups: any = {};
        freeSlots.forEach(slot => {
          if (!groups[slot.doctor.id]) {
            groups[slot.doctor.id] = { doctor: slot.doctor, slots: [] };
          }
          groups[slot.doctor.id].slots.push(slot);
        });

        this.groupedSlots = Object.values(groups);
        this.selectedSlotId = null;
      });
  }

  selectSlot(slotId: number) {
    this.selectedSlotId = slotId;
  }

  confirmAppointment() {
  if (!this.selectedSlotId) return alert("Please select a slot first");
  if (!this.selectedServiceId) return alert("Please select a service first");

  this.http.post('http://localhost:5050/patient/appointments/book', null, {
    params: {
      slotId: this.selectedSlotId,
      serviceId: this.selectedServiceId
    },
    ...this.getAuthHeaders()
  }).subscribe({
    next: res => {
      alert('Appointment booked successfully!');

      this.selectedDepartmentId = null;
      this.selectedServiceId = null;
      this.services = [];
      this.selectedDate = new Date();
      this.groupedSlots = [];
      this.selectedSlotId = null;

      this.loadDepartments();
    },
    error: err => {
      console.log(err);
      alert('Error booking appointment');
    }
  });
}
}
