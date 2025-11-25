import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Alert } from '../../services/alert';

interface Department { id: number; name: string; }
interface ClinicService { id: number; name: string; price: number; department: Department; }
interface Doctor { id: number; user: { firstName: string; lastName: string; phone: string; }; title: string; }
interface Slot { id: number; doctor: Doctor; date: string; startTime: string; endTime: string; booked: boolean; }
interface Appointment { id: number; doctor: Doctor; service: ClinicService; doctorSchedule: Slot; status: string; }

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patient-dashboard.html',
  styleUrls: ['./patient-dashboard.css'],
})
export class PatientDashboard implements OnInit {

  departments: Department[] = [];
  services: ClinicService[] = [];
  appointments: Appointment[] = [];
  groupedSlots: { doctor: Doctor; slots: Slot[] }[] = [];
  
  selectedDepartmentId: number | null = null;
  selectedServiceId: number | null = null;
  selectedSlotId: number | null = null;
  selectedDate: Date = new Date();
  
  showAppointments = false;

  constructor(private router: Router, private http: HttpClient, private alert: Alert) { }

  ngOnInit() {
    this.loadDepartments();
  }

  get formattedDate(): string {
    return this.selectedDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' });
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) };
  }

  private getIsoDateString(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  loadDepartments() {
    this.http.get<Department[]>('http://localhost:5050/patient/departments', this.getAuthHeaders())
      .subscribe(res => this.departments = res);
  }

  onDepartmentChange() {
    this.selectedServiceId = null;
    this.services = [];
    this.selectedDate = new Date();
    this.groupedSlots = [];
    this.selectedSlotId = null;

    if (!this.selectedDepartmentId) return;

    this.http.get<ClinicService[]>(`http://localhost:5050/patient/departments/${this.selectedDepartmentId}/services`, this.getAuthHeaders())
      .subscribe(res => this.services = res);
  }

  loadSlots() {
    if (!this.selectedDepartmentId || !this.selectedServiceId) return;

    const dateStr = this.getIsoDateString(this.selectedDate);

    this.http.get<Slot[]>(`http://localhost:5050/patient/departments/${this.selectedDepartmentId}/services/${this.selectedServiceId}/slots?date=${dateStr}`, this.getAuthHeaders())
      .subscribe({
        next: (res) => {
          const freeSlots = res.filter(s => !s.booked);
          
          const groups: any = {};
          freeSlots.forEach(slot => {
            if (!groups[slot.doctor.id]) {
              groups[slot.doctor.id] = { doctor: slot.doctor, slots: [] };
            }
            groups[slot.doctor.id].slots.push(slot);
          });

          this.groupedSlots = Object.values(groups);
          this.selectedSlotId = null;
        },
        error: (err) => console.error(err)
      });
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

  selectSlot(slotId: number) {
    this.selectedSlotId = slotId;
  }

  confirmAppointment() {
    if (!this.selectedSlotId) return this.alert.error("Please select a time slot");
    if (!this.selectedServiceId) return this.alert.error("Please select a service");

    this.http.post('http://localhost:5050/patient/appointments/book', null, {
      params: { slotId: this.selectedSlotId, serviceId: this.selectedServiceId },
      ...this.getAuthHeaders()
    }).subscribe({
      next: () => {
        this.alert.success('Appointment booked successfully!');
        this.selectedSlotId = null;
        this.loadSlots();
        if (this.showAppointments) this.loadAppointments(); 
      },
      error: (err) => {
        console.error(err);
        this.alert.error(err.error?.error || 'Booking failed');
      }
    });
  }

  toggleAppointments() {
    this.showAppointments = !this.showAppointments;
    if (this.showAppointments) this.loadAppointments();
  }

  loadAppointments() {
    this.http.get<Appointment[]>('http://localhost:5050/patient/appointments/list', this.getAuthHeaders())
      .subscribe(res => this.appointments = res);
  }

  cancelAppointment(id: number) {
    if (!confirm("Are you sure you want to cancel?")) return;

    this.http.delete(`http://localhost:5050/patient/appointments/${id}`, this.getAuthHeaders())
      .subscribe({
        next: () => {
          this.alert.success("Appointment cancelled");
          this.loadAppointments();
          this.loadSlots();
        },
        error: (err) => this.alert.error(err.error?.error || 'Cancellation failed')
      });
  }
}