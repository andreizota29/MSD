package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Staff;
import com.uaic.mediconnect.entity.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffScheduleRepo extends JpaRepository<StaffSchedule, Long> {
    List<StaffSchedule> findByStaff_StaffId(Long staffId);
}
