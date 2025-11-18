package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@DataJpaTest
public class AppointmentRepoTest {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Patient testPatient;
    private Doctor testDoctor;
    private ClinicService testService;
    private DoctorSchedule testSlot;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setName("General");
        entityManager.persist(dept);

        User docUser = new User();
        docUser.setFirstName("Doc");
        docUser.setLastName("Tor");
        docUser.setPassword("StrongPass1!");
        docUser.setPhone("0744111222");
        docUser.setEmail("d@d.com");
        docUser.setRole(Role.DOCTOR);
        entityManager.persist(docUser);

        testDoctor = new Doctor();
        testDoctor.setUser(docUser);
        testDoctor.setDepartment(dept);
        testDoctor.setTitle("MD");
        testDoctor.setActive(true);
        testDoctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_8_17);
        entityManager.persist(testDoctor);

        testService = new ClinicService();
        testService.setName("Exam");
        testService.setPrice(100.0);
        testService.setDepartment(dept);
        entityManager.persist(testService);

        User patUser = new User();
        patUser.setFirstName("Pat");
        patUser.setLastName("Ient");
        patUser.setPassword("StrongPass1!");
        patUser.setPhone("0755333444");
        patUser.setEmail("p@p.com");
        patUser.setRole(Role.PATIENT);
        entityManager.persist(patUser);

        testPatient = new Patient();
        testPatient.setUser(patUser);
        testPatient.setCnp("1234567890123");
        testPatient.setDateOfBirth(LocalDate.now());
        entityManager.persist(testPatient);

        testSlot = new DoctorSchedule();
        testSlot.setDoctor(testDoctor);
        testSlot.setDate(LocalDate.now());
        testSlot.setStartTime(LocalTime.of(10, 0));
        testSlot.setBooked(false);
        entityManager.persist(testSlot);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should save an appointment")
    void testSaveAppointment() {
        Appointment appointment = new Appointment();
        appointment.setDoctor(testDoctor);
        appointment.setPatient(testPatient);
        appointment.setService(testService);
        appointment.setDoctorSchedule(testSlot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepo.save(appointment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(saved.getPatient().getUser().getEmail()).isEqualTo("p@p.com");
    }
}
