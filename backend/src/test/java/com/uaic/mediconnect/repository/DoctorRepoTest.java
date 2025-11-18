package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DoctorRepoTest {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private TestEntityManager entityManager;

    private User doctorUser;
    private Department department;

    @BeforeEach
    void setUp() {
        doctorUser = new User("Last", "First", "pass", "0711", "doc@test.com", Role.DOCTOR);
        entityManager.persist(doctorUser);

        department = new Department();
        department.setName("Neurology");
        entityManager.persist(department);
        entityManager.flush();
    }

    @Test
    void testSaveDoctor() {
        Doctor doctor = new Doctor();
        doctor.setUser(doctorUser);
        doctor.setDepartment(department);
        doctor.setTitle("Medic Primar");
        doctor.setActive(true);
        doctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_9_18);

        Doctor saved = doctorRepo.save(doctor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getEmail()).isEqualTo("doc@test.com");
        assertThat(saved.getDepartment().getName()).isEqualTo("Neurology");
    }


}
