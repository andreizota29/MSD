package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Pacient;
import com.uaic.mediconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacientRepo extends JpaRepository<Pacient, Long> {
    Optional<Pacient> findByUserId(Long userId);
    Optional<Pacient> findByUser(User user);
}
