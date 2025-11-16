package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicServiceRepo extends JpaRepository<ClinicService, Long> {
    List<ClinicService> findByDepartment(Department department);
}