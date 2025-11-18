import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Alert } from '../../services/alert';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './doctor-dashboard.html',
  styleUrl: './doctor-dashboard.css',
})
export class DoctorDashboard implements OnInit {

  weekSlots: any[] = [];
  weekStart!: Date;
  weekDays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];
  minDate!: Date;

  times: string[] = [];
  slotsByDateTime: Map<string, Map<string, any>> = new Map();

  constructor(private http: HttpClient, private router: Router, private alert: Alert) { }

  get canGoBack(): boolean {
    return this.weekStart.getTime() > this.minDate.getTime();
  }

  ngOnInit() {
    this.initWeek();
    this.minDate = new Date(this.weekStart);
    this.loadWeek();
  }

  goToChangePassword() {
  this.router.navigate(['/change-password']);
}

  initWeek() {
    const today = new Date();
    const day = today.getDay();
    const mondayOffset = (day + 6) % 7;
    this.weekStart = new Date(today);
    this.weekStart.setHours(0, 0, 0, 0);
    this.weekStart.setDate(today.getDate() - mondayOffset);
  }

  changeWeek(offset: number) {
    if (offset < 0 && !this.canGoBack) return;
    this.weekStart = new Date(this.weekStart.getTime() + offset * 7 * 24 * 60 * 60 * 1000);
    this.loadWeek();
  }

  loadWeek() {
    const headers = this.getAuthHeaders();
    const dateStr = this.formatDate(this.weekStart);
    this.http.get<any[]>(`http://localhost:5050/doctor/timetable/week?start=${dateStr}`, { headers })
      .subscribe({
        next: res => {
          this.weekSlots = res ?? [];
          this.prepareViewData();
        },
        error: err => {
          console.error('Error loading week slots', err);
          this.weekSlots = [];
          this.prepareViewData();
        }
      });
  }

  prepareViewData() {
    this.times = [];
    this.slotsByDateTime = new Map();
    const timeSet = new Set<string>();

    for (const s of this.weekSlots) {
      const date = s.date;
      const start = this.normalizeTimeString(s.startTime);
      const end = this.normalizeTimeString(s.endTime);

      if (!this.slotsByDateTime.has(date)) this.slotsByDateTime.set(date, new Map());
      s._start = start;
      s._end = end;
      this.slotsByDateTime.get(date)!.set(start, s);

      timeSet.add(start);
    }

    const allTimes: string[] = [];
    if (timeSet.size > 0) {
      const sorted = Array.from(timeSet).sort();
      let cur = sorted[0];
      const last = sorted[sorted.length - 1];
      while (cur <= last) {
        allTimes.push(cur);
        cur = this.addMinutes(cur, 30);
      }
    }
    this.times = allTimes;

    const today = new Date();
    const workingDays = this.getDoctorWorkingDays();

    for (let i = 0; i < 7; i++) {
      const d = this.getWeekDate(i);
      const ds = this.formatDate(d);

      if (!this.slotsByDateTime.has(ds)) this.slotsByDateTime.set(ds, new Map());

      const dayName = this.weekDays[i];
      if (workingDays.includes(dayName) && d >= today) {
        const map = this.slotsByDateTime.get(ds)!;
        for (const t of allTimes) {
          if (!map.has(t)) map.set(t, null);
        }
      }
    }
  }

  getDoctorWorkingDays(): string[] {
    return ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
  }

  getSlot(dayIndex: number, time: string) {
    const dateStr = this.formatDate(this.getWeekDate(dayIndex));
    const dayMap = this.slotsByDateTime.get(dateStr);
    return dayMap ? dayMap.get(time) ?? null : null;
  }

  getWeekDate(offset: number): Date {
    const d = new Date(this.weekStart);
    d.setDate(this.weekStart.getDate() + offset);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  formatDate(d: Date): string {
    return d.toLocaleDateString('en-CA');
  }

  normalizeTimeString(t: string | undefined | null): string {
    if (!t) return '';
    const hhmm = t.split('T').pop()?.split('.')[0];
    const parts = (hhmm ?? t).split(':');
    if (parts.length >= 2) {
      const hh = parts[0].padStart(2, '0');
      const mm = parts[1].padStart(2, '0');
      return `${hh}:${mm}`;
    }
    return t;
  }

  isPast(slot: any) {
    if (!slot) return false;
    const now = new Date();
    const dt = new Date(`${slot.date}T${slot._end || this.normalizeTimeString(slot.endTime)}:00`);
    return dt.getTime() < now.getTime();
  }

  slotStatus(slot: any): string {
    if (!slot) return '';

    if (slot.booked) {
      const details = this.getPatientAndService(slot);
      
      if (this.isPast(slot)) {
        return `Completed – ${details}`; 
      }
      return details; 
    }

    if (this.isPast(slot)) return 'Past'; 
    return 'Free'; 
  }

  getPatientAndService(slot: any): string {
    if (!slot || !slot.patient) return 'Booked';

    const p = slot.patient;
    let name = '';
    
    if (p.user) {
      name = `${p.user.firstName ?? ''} ${p.user.lastName ?? ''}`.trim();
    } else {
      name = `${p.firstName ?? ''} ${p.lastName ?? ''}`.trim();
    }
    const service = slot.serviceName ? ` (${slot.serviceName})` : '';

    return name + service;
  }

  isBooked(slot: any) {
    return slot && !!slot.booked;
  }

  slotLabel(slot: any) {
    if (!slot) return '';
    return `${slot._start} – ${slot._end}`;
  }

  slotPatientName(slot: any) {
    if (!slot || !slot.patient) return '';
    const p = slot.patient;
    if (p.user) return `${p.user.firstName ?? ''} ${p.user.lastName ?? ''}`.trim();
    return `${p.firstName ?? ''} ${p.lastName ?? ''}`.trim();
  }

  addMinutes(time: string, minutes: number): string {
    const [h, m] = time.split(':').map(Number);
    const d = new Date();
    d.setHours(h, m + minutes);
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
