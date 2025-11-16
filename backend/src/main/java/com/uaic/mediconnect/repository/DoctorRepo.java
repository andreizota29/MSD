package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {
    List<Doctor> findByDepartment(Department department);
    Optional<Doctor> findByUser_UserId(Long userId);
    List<Doctor> findByActiveTrue();
    Optional<Doctor> findByUser_UserIdAndActiveTrue(Long userId);

}
