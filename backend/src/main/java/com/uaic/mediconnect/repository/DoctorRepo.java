package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.DepartmentType;
import com.uaic.mediconnect.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialty(String specialty);
    List<Doctor> findByDepartment(DepartmentType department);
    Optional<Doctor> findByUser_UserId(Long userId);

}
