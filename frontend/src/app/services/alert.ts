import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export interface AlertData {
  type: 'success' | 'error' | 'info' | 'confirm';
  title: string;
  message: string;
  confirmCallback?: () => void; 
}



@Injectable({
  providedIn: 'root',
})
export class Alert {
  public alertState = new BehaviorSubject<AlertData | null>(null);

  constructor() {}

  success(message: string, title: string = 'Success') {
    this.alertState.next({ type: 'success', title, message });
  }

  error(message: string, title: string = 'Error') {
    this.alertState.next({ type: 'error', title, message });
  }

  confirm(message: string, onConfirm: () => void) {
    this.alertState.next({ 
      type: 'confirm', 
      title: 'Are you sure?', 
      message, 
      confirmCallback: onConfirm 
    });
  }
  close() {
    this.alertState.next(null);
  }
}
