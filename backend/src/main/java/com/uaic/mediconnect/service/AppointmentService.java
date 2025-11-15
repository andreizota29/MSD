package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.repository.AppointmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;

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
}
