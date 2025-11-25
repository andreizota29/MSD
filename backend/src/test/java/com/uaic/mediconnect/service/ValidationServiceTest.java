package com.uaic.mediconnect.service;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.exception.BusinessValidationException;
import com.uaic.mediconnect.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ValidationServiceTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ValidationService validationService;

    private Long patientId;
    private Long serviceId;
    private Long slotId;
    private Long userId;

    @BeforeEach
    void setUp() {
        Department dept = new Department();
        dept.setName("D");
        departmentRepo.save(dept);

        User docUser = new User("Doc", "Val", "Pass123!", "0711151111", "doc@val.com", Role.DOCTOR);
        userRepo.save(docUser);
        Doctor doctor = new Doctor();
        doctor.setUser(docUser);
        doctor.setDepartment(dept);
        doctor.setTitle("Dr");
        doctor.setActive(true);
        doctorRepo.save(doctor);

        ClinicService service = new ClinicService();
        service.setName("TestService");
        service.setPrice(200.0);
        service.setDepartment(dept);
        serviceRepo.save(service);
        this.serviceId = service.getId();

        DoctorSchedule slot = new DoctorSchedule();
        slot.setDoctor(doctor);
        slot.setDate(LocalDate.now().plusDays(1));
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(10, 30));
        slot.setBooked(false);
        scheduleRepo.save(slot);
        this.slotId = slot.getId();

        User patUser = new User("Pat", "Val", "Pass123!", "0726222222", "pat@val.com", Role.PATIENT);
        patUser.setProfileCompleted(true);
        userRepo.save(patUser);
        this.userId = patUser.getUserId();

        Patient patient = new Patient();
        patient.setUser(patUser);
        patient.setCnp("5020905226760");
        patient.setDateOfBirth(LocalDate.of(2002, 9, 5));
        patientRepo.save(patient);
        this.patientId = patient.getId();
    }

    @Test
    @DisplayName("Booking should succeed when everything is valid")
    void testValidateAppointmentBookingSuccess() {
        assertDoesNotThrow(() ->
                validationService.validateAppointmentBooking(slotId, serviceId, patientId)
        );
    }

    @Test
    @DisplayName("Booking should fail if slot is already booked")
    void testValidateAppointmentBookingSlotAlreadyBooked() {
        DoctorSchedule slot = scheduleRepo.findById(slotId).get();
        slot.setBooked(true);
        scheduleRepo.save(slot);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> {
            validationService.validateAppointmentBooking(slotId, serviceId, patientId);
        });
        assertEquals("This time slot is already booked.", ex.getMessage());
    }

    @Test
    @DisplayName("Booking should fail if slot is in the past")
    void testValidateAppointmentBookingTimeInPast() {
        DoctorSchedule slot = scheduleRepo.findById(slotId).get();
        slot.setDate(LocalDate.now().minusDays(1));
        scheduleRepo.save(slot);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> {
            validationService.validateAppointmentBooking(slotId, serviceId, patientId);
        });
        assertEquals("Cannot book an appointment in the past.", ex.getMessage());
    }

    @Test
    @DisplayName("Booking should fail if service department does not match doctor department")
    void testValidateAppointmentBookingDepartmentMismatch() {
        Department otherDept = new Department();
        otherDept.setName("Other");
        departmentRepo.save(otherDept);

        ClinicService otherService = new ClinicService();
        otherService.setName("OtherService");
        otherService.setPrice(50.0);
        otherService.setDepartment(otherDept);
        serviceRepo.save(otherService);

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> {
            validationService.validateAppointmentBooking(slotId, otherService.getId(), patientId);
        });
        assertEquals("The selected service is not provided in this doctor's department", ex.getMessage());
    }
    @Test
    void testValidateCancellationSuccess() {
        Patient patient = patientRepo.findById(patientId).get();
        DoctorSchedule slot = scheduleRepo.findById(slotId).get();
        ClinicService service = serviceRepo.findById(serviceId).get();

        Appointment app = new Appointment();
        app.setPatient(patient);
        app.setDoctor(slot.getDoctor());
        app.setDoctorSchedule(slot);
        app.setService(service);
        app.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(app);

        assertDoesNotThrow(() ->
                validationService.validateAppointmentCancellation(app.getId(), userId)
        );
    }

    @Test
    void testValidateCancellationWrongUser() {
        Patient patient = patientRepo.findById(patientId).get();
        DoctorSchedule slot = scheduleRepo.findById(slotId).get();

        Appointment app = new Appointment();
        app.setPatient(patient);
        app.setDoctor(slot.getDoctor());
        app.setDoctorSchedule(slot);
        app.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(app);
        assertThrows(BusinessValidationException.class, () ->
                validationService.validateAppointmentCancellation(app.getId(), 999L)
        );
    }

    @Test
    @DisplayName("Profile validation should pass for valid CNP and matching Date")
    void testValidatePatientProfileDataSuccess() {
        Patient p = new Patient();
        p.setCnp("5020905226760");
        p.setDateOfBirth(LocalDate.of(2002, 9, 5));

        assertDoesNotThrow(() -> validationService.validatePatientProfileData(p));
    }

    @Test
    @DisplayName("Profile validation should fail for invalid length CNP")
    void testValidatePatientProfileDataInvalidLength() {
        Patient p = new Patient();
        p.setCnp("123");
        p.setDateOfBirth(LocalDate.now());

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                validationService.validatePatientProfileData(p)
        );
        assertEquals("CNP must be exactly 13 digits.", ex.getMessage());
    }

    @Test
    @DisplayName("Profile validation should fail for bad Checksum")
    void testValidatePatientProfileDataBadChecksum() {
        Patient p = new Patient();
        p.setCnp("5020905226769");
        p.setDateOfBirth(LocalDate.of(2002, 9, 5));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                validationService.validatePatientProfileData(p)
        );
        assertEquals("Invalid CNP: Control digit does not match.", ex.getMessage());
    }

    @Test
    @DisplayName("Profile validation should fail if CNP date does not match DOB")
    void testValidatePatientProfileDataDateMismatch() {
        Patient p = new Patient();
        p.setCnp("5020905226760");
        p.setDateOfBirth(LocalDate.of(1999, 1, 1));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                validationService.validatePatientProfileData(p)
        );
        assertTrue(ex.getMessage().contains("does not match"));
    }
}
