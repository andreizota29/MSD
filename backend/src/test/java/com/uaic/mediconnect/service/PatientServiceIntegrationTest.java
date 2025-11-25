package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.*;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

@SpringBootTest
@Transactional
public class PatientServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Test
    @DisplayName("Deleting patient should remove appointments but keep schedule slot")
    void testDeletePatientFully() {
        Department dept = new Department();
        dept.setName("Dep");
        departmentRepo.save(dept);

        User docUser = new User("Doc", "X", "Pas233s1!", "0721111111", "dx@x.com", Role.DOCTOR);
        userRepo.save(docUser);
        Doctor doctor = new Doctor();
        doctor.setUser(docUser);
        doctor.setDepartment(dept);
        doctor.setTitle("Tit");
        doctorRepo.save(doctor);

        User patUser = new User("Pat", "X", "Pas123s1!", "0731111111", "px@x.com", Role.PATIENT);
        userRepo.save(patUser);
        Patient patient = new Patient();
        patient.setUser(patUser);
        patient.setCnp("5020905226760");
        patient.setDateOfBirth(LocalDate.of(2002,9,5));
        patientRepo.save(patient);

        DoctorSchedule slot = new DoctorSchedule();
        slot.setDoctor(doctor);
        slot.setPatient(patient);
        slot.setBooked(true);
        scheduleRepo.save(slot);

        Appointment app = new Appointment();
        app.setPatient(patient);
        app.setDoctor(doctor);
        app.setDoctorSchedule(slot);
        app.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(app);

        patientService.deletePatientFully(patUser);

        assertThat(patientRepo.findById(patient.getId())).isEmpty();
        assertThat(userRepo.findById(patUser.getUserId())).isEmpty();

        assertThat(appointmentRepo.findById(app.getId())).isEmpty();

        Optional<DoctorSchedule> savedSlot = scheduleRepo.findById(slot.getId());
        assertThat(savedSlot.get().isBooked()).isFalse();
        assertThat(savedSlot.get().getPatient()).isNull();

    }

}
