package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Staff;
import com.uaic.mediconnect.entity.StaffScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StaffScheduleExceptionRepo extends JpaRepository<StaffScheduleException, Long> {
    List<StaffScheduleException> findByStaff_StaffId(Long staffId);
}
