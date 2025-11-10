package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicRepo extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByName(String name);
}
