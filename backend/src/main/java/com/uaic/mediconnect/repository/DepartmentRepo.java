package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepo extends JpaRepository<Department, Long> { }