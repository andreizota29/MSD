package com.uaic.mediconnect.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorScheduleDTO {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean booked;
    private DoctorDTO doctor;
    private PatientDTO patient;
    private String serviceName;
}
