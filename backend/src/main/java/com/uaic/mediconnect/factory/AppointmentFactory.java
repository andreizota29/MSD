package com.uaic.mediconnect.factory;

import com.uaic.mediconnect.entity.*;
import org.springframework.stereotype.Component;

@Component
public class AppointmentFactory {

    public Appointment createAppointment(Patient patient, DoctorSchedule slot, ClinicService service) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(slot.getDoctor());
        appointment.setDoctorSchedule(slot);
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointment;
    }
}
