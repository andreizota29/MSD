package com.uaic.mediconnect.dto;

import com.uaic.mediconnect.entity.AppointmentStatus;
import lombok.Data;

@Data
public class AppointmentDTO {
    private Long id;
    private AppointmentStatus status;
    private DoctorDTO doctor;
    private PatientDTO patient;
    private ClinicServiceDTO service;
    private DoctorScheduleDTO doctorSchedule;
}
