package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface DoctorScheduleRepo extends JpaRepository<DoctorSchedule, Long> {
    List<DoctorSchedule> findByDoctorOrderByDateAscStartTimeAsc(Doctor doctor);
    List<DoctorSchedule> findByDoctorAndDateOrderByStartTimeAsc(Doctor d, LocalDate targetDate);
    List<DoctorSchedule> findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(Doctor doctor, LocalDate monday, LocalDate sunday);
    Collection<? extends DoctorSchedule> findByDoctorAndDateAndBookedFalseOrderByStartTimeAsc(Doctor d, LocalDate targetDate);
    void deleteAllByDoctor(Doctor doctor);
}
