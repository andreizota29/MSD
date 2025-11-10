package com.uaic.mediconnect.entity;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name="nurse")
public class Nurse extends Staff{

    @ManyToOne
    @JoinColumn(name = "supervising_doctor_id")
    private Doctor supervisingDoctor;

    @ManyToOne
    @JoinColumn(name="clinic_id")
    private Clinic clinic;


    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffScheduleException> scheduleExceptions;
}
