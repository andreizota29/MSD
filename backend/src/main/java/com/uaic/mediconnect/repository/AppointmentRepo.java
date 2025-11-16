package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctor(Doctor doctor);
    List<Appointment> findByPatient(Patient patient);
    void deleteAllByDoctorSchedule(DoctorSchedule schedule);
    boolean existsByDoctor(Doctor doctor);
    List<Appointment> findByService(ClinicService service);
}