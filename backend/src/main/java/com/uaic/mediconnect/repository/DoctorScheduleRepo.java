package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorScheduleRepo extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorOrderByDateAscStartTimeAsc(Doctor doctor);
}
