package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.AppointmentStatus;
import com.uaic.mediconnect.repository.AppointmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AppointmentScheduler {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void markCompletedAppointments() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Bucharest"));
        List<Appointment> activeAppointments = appointmentRepo.findByStatus(AppointmentStatus.SCHEDULED);
        for(Appointment app : activeAppointments) {
            LocalDateTime appointmentEndTime = LocalDateTime.of(
                    app.getDoctorSchedule().getDate(),
                    app.getDoctorSchedule().getEndTime()
            );
            if(appointmentEndTime.isBefore(now)){
                app.setStatus(AppointmentStatus.COMPLETED);
                appointmentRepo.save(app);
                System.out.println("Auto-completed appointment ID: " + app.getId());
            }
        }
    }
}
