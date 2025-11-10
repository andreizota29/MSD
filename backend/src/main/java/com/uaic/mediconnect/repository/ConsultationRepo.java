package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Consultation;
import com.uaic.mediconnect.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultationRepo extends JpaRepository<Consultation,Long> {
    List<Consultation> findByDoctorAndDateTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);
}
