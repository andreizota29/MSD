package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class AppointmentRepoTest {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save an appointment into the database")
    void testSaveAppointment(){
        User userPatient = new User();
        userPatient.setEmail("app@patient.com");
        userPatient.setPassword("patient123");
        userPatient.setRole(Role.PATIENT);
        userPatient.setFirstName("Appoint");
        userPatient.setLastName("Ment");
        userPatient.setProfileCompleted(true);
        entityManager.persist(userPatient);
        entityManager.flush();

        User userDoctor = new User();
        userDoctor.setEmail("app@doctor.com");
        userDoctor.setPassword("doctor123");
        userDoctor.setRole(Role.DOCTOR);
        userDoctor.setFirstName("Docto");
        userDoctor.setLastName("Rul");
        userDoctor.setProfileCompleted(false);
        entityManager.persist(userDoctor);
        entityManager.flush();

        Patient patient = new Patient();
        patient.setUser(userPatient);
        patient.setCnp("123123123123");
        patient.setDateOfBirth(LocalDate.of(2002,2,2));
        entityManager.persist(patient);
        entityManager.flush();

        Department department = new Department();
        department.setName("Departant");

        Doctor doctor = new Doctor();
        doctor.setUser(userDoctor);
        doctor.setTitle("Doctorand");
        doctor.setDepartment(department);
        doctor.setActive(true);
        doctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_8_17);

        ClinicService clinicService = new ClinicService();
        clinicService.setName("ServName");
        clinicService.setPrice(200.00);

        clinicService.setDepartment(department);
        doctor.setDepartment(department);

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPatient(patient);

    }
}
