package com.uaic.mediconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="staff_schedule_exception")
public class StaffScheduleException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exceptionId;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private LocalTime overrideStartTime;

    @Column
    private LocalTime overrideEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExceptionType type;
}
