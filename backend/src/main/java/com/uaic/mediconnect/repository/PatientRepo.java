package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepo extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUser(User user);
}
