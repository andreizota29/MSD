import { Component } from '@angular/core';
import { Alert, AlertData } from '../../services/alert';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-custom-alert',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './custom-alert.html',
  styleUrl: './custom-alert.css',
})
export class CustomAlert {
  data: AlertData | null = null;

  constructor(private alertService: Alert) {
    this.alertService.alertState.subscribe(res => this.data = res);
  }

  close() {
    this.alertService.close();
  }

  confirm() {
    if (this.data?.confirmCallback) {
      this.data.confirmCallback();
    }
    this.close();
  }
}
