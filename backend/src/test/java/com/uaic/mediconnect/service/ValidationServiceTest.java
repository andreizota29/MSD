package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.exception.BusinessValidationException;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.PatientRepo;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest {

    @Mock
    private DoctorScheduleRepo scheduleRepo;

    @Mock
    private ClinicServiceRepo serviceRepo;

    @Mock
    private PatientRepo patientRepo;

    @Mock
    private AppointmentRepo appointmentRepo;

    @InjectMocks
    private ValidationServiceImpl validationService;

    private DoctorSchedule slot;
    private ClinicService service;
    private Patient patient;
    private Department department;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("TestDep");

        doctor = new Doctor();
        doctor.setId(10L);
        doctor.setDepartment(department);

        slot = new DoctorSchedule();
        slot.setId(100L);
        slot.setBooked(false);
        slot.setDoctor(doctor);

        service = new ClinicService();
        service.setId(50L);
        service.setDepartment(department);

        patient = new Patient();
        patient.setId(1L);
    }

    @Test
    void testValidateAppointmentBookingSuccess() {
        when(patientRepo.existsById(1L)).thenReturn(true);
        when(scheduleRepo.findById(100L)).thenReturn(Optional.of(slot));
        when(serviceRepo.findById(50L)).thenReturn(Optional.of(service));

        assertDoesNotThrow(() ->
                validationService.validateAppointmentBooking(100L, 50L, 1L)
                );
    }

    @Test
    void testValidateAppointmentBookingSlotAlreadyBooked() {
        slot.setBooked(true);
        when(patientRepo.existsById(1L)).thenReturn(true);
        when(scheduleRepo.findById(100L)).thenReturn(Optional.of(slot));

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            validationService.validateAppointmentBooking(100L, 50L, 1L);
        });

        assertEquals("This time slot is already booked.", exception.getMessage());
    }

    @Test
    void testValidateAppointmentBookingDepartmentMismatch() {
        Department neuroDept = new Department();
        neuroDept.setId(2L);
        service.setDepartment(neuroDept);

        when(patientRepo.existsById(1L)).thenReturn(true);
        when(scheduleRepo.findById(100L)).thenReturn(Optional.of(slot));
        when(serviceRepo.findById(50L)).thenReturn(Optional.of(service));

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            validationService.validateAppointmentBooking(100L, 50L, 1L);
        });

        assertEquals("The selected service is not provided in this doctor's department", exception.getMessage());
    }

    @Test
    void testValidateCancellationSuccess(){
        User user = new User();
        user.setUserId(99L);

        patient.setUser(user);

        Appointment appt = new Appointment();
        appt.setId(500L);
        appt.setPatient(patient);
        appt.setStatus(AppointmentStatus.SCHEDULED);

        when(appointmentRepo.findById(500L)).thenReturn(Optional.of(appt));

        assertDoesNotThrow(() -> validationService.validateAppointmentCancellation(500L, 99L));
    }

    @Test
    void testValidateCancellationWrongUser(){
        User user = new User();
        user.setUserId(99L);
        patient.setUser(user);

        Appointment appt = new Appointment();
        appt.setId(500L);
        appt.setPatient(patient);

        when(appointmentRepo.findById(500L)).thenReturn(Optional.of(appt));
        assertThrows(BusinessValidationException.class, () ->
                validationService.validateAppointmentCancellation(500L, 888L)
        );
    }
}
