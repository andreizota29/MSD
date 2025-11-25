package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
public class DoctorScheduleRepoTest {

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindAvailableSlots(){
        User user = new User("Doc", "Time", "Parola123@", "0733272222", "time@doc.com", Role.DOCTOR);
        entityManager.persist(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setTitle("Dr Time");
        entityManager.persist(doctor);

        LocalDate today = LocalDate.now();

        DoctorSchedule s1 = new DoctorSchedule();
        s1.setDoctor(doctor);
        s1.setDate(today);
        s1.setStartTime(LocalTime.of(9, 0));
        s1.setBooked(false);
        entityManager.persist(s1);

        DoctorSchedule s2 = new DoctorSchedule();
        s2.setDoctor(doctor);
        s2.setDate(today);
        s2.setStartTime(LocalTime.of(10, 0));
        s2.setBooked(true); // Booked
        entityManager.persist(s2);

        entityManager.flush();

        List<DoctorSchedule> freeSlots = scheduleRepo.findByDoctorAndDateAndBookedFalseOrderByStartTimeAsc(doctor, today);

        assertThat(freeSlots).hasSize(1);
        assertThat(freeSlots.get(0).getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }
}
