package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.*;
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
import java.time.LocalTime;

@SpringBootTest
@Transactional
public class AppointmentServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private UserRepo userRepo;

    private Long patientId;
    private Long serviceId;
    private Long slotId;
    private Long userId;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setName("General");
        departmentRepo.save(dept);

        User docUser = new User("Doc", "Real", "Strong123!", "0744444444", "dr@real.com", Role.DOCTOR);
        userRepo.save(docUser);
        Doctor doctor = new Doctor();
        doctor.setUser(docUser);
        doctor.setDepartment(dept);
        doctor.setTitle("Med");
        doctorRepo.save(doctor);

        DoctorSchedule slot = new DoctorSchedule();
        slot.setDoctor(doctor);
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(10,0));
        slot.setBooked(false);
        scheduleRepo.save(slot);
        this.slotId = slot.getId();

        ClinicService service = new ClinicService();
        service.setName("Checkup");
        service.setPrice(150.0);
        service.setDepartment(dept);
        serviceRepo.save(service);
        this.serviceId = service.getId();

        User patUser = new User("Pat", "Real", "Stronggg12!" , "0755555555", "p@real.com", Role.PATIENT);
        patUser.setProfileCompleted(true);
        userRepo.save(patUser);
        this.userId = patUser.getUserId();

        Patient patient = new Patient();
        patient.setUser(patUser);
        patient.setCnp("5020905226760");
        patient.setDateOfBirth(LocalDate.of(20002,9,5));
        patientRepo.save(patient);
        this.patientId = patient.getId();
    }

    @Test
    @DisplayName("Book appointment then cancel it")
    void testBookAndCancelApp() {
        Long appointmentId = appointmentService.bookAppointmentFully(slotId, serviceId, patientId);
        assertThat(appointmentId).isNotNull();

        DoctorSchedule slotAfterBook = scheduleRepo.findById(slotId).get();
        assertThat(slotAfterBook.isBooked()).isTrue();
        assertThat(slotAfterBook.getPatient().getId()).isEqualTo(patientId);

        Appointment savedApp = appointmentRepo.findById(appointmentId).get();
        assertThat(savedApp.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);

        appointmentService.cancelAppointmentFully(appointmentId, userId);

        Appointment cancelledApp = appointmentRepo.findById(appointmentId).get();
        assertThat(cancelledApp.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);

        DoctorSchedule slotAfterCancel = scheduleRepo.findById(slotId).get();
        assertThat(slotAfterCancel.isBooked()).isFalse();
        assertThat(slotAfterCancel.getPatient()).isNull();
    }
}
