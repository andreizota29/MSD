package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    public List<Appointment> findByPatient(Patient patient) {
        return appointmentRepo.findByPatient(patient);
    }

    public List<Appointment> findByDoctor(Doctor doctor){
        return appointmentRepo.findByDoctor(doctor);
    }

    public Appointment save(Appointment appointment) {
        return appointmentRepo.save(appointment);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepo.findById(id);
    }

    public void delete(Appointment appointment) {
        appointmentRepo.delete(appointment);
    }

    public boolean existsByDoctor(Doctor doctor) {
        return appointmentRepo.existsByDoctor(doctor);
    }

    @Transactional
    public void cancelAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appointment);
        freeSlot(appointment);
    }

    private void freeSlot(Appointment appointment) {
        DoctorSchedule slot = appointment.getDoctorSchedule();
        if (slot != null) {
            slot.setBooked(false);
            slot.setPatient(null);
            scheduleRepo.save(slot);
        }
    }

    public List<Appointment> findByService(ClinicService service){
        return appointmentRepo.findByService(service);
    }
}
