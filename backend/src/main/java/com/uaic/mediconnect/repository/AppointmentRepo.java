package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByPatient(Patient patient);
}