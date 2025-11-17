package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.AppointmentStatus;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentServiceTest {

    @Mock
    private AppointmentRepo appointmentRepo;

    @Mock
    private DoctorScheduleRepo scheduleRepo;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment appointment;
    private DoctorSchedule schedule;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        schedule = new DoctorSchedule();
        schedule.setId(1L);
        schedule.setBooked(true);
        schedule.setDate(LocalDate.now());
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(10, 30));
        schedule.setPatient(new Patient());

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctorSchedule(schedule);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
    }

    @Test
    void testCancelAppointment() {
        appointmentService.cancelAppointment(appointment);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());

        verify(appointmentRepo, times(1)).save(appointment);

        ArgumentCaptor<DoctorSchedule> captor = ArgumentCaptor.forClass(DoctorSchedule.class);
        verify(scheduleRepo, times(1)).save(captor.capture());

        DoctorSchedule savedSchedule = captor.getValue();

        assertFalse(savedSchedule.isBooked());
        assertNull(savedSchedule.getPatient());
    }
}
