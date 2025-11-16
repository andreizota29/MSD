package com.uaic.mediconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonIgnoreProperties({"schedules"})
    private Doctor doctor;

    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    private boolean booked = false;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({"appointments"})
    private Patient patient;
}
