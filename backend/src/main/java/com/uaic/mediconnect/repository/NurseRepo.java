package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Nurse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NurseRepo extends JpaRepository<Nurse, Long> {
    List<Nurse> findBySupervisingDoctor(Doctor doctor);
}
