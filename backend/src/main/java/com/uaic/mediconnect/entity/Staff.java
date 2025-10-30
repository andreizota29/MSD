package com.uaic.mediconnect.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer staffId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String department;
    private LocalDate hireDate;
    private Boolean isOnDuty = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole staffRole;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StaffAvailability> availabilities = new ArrayList<>();

    public enum StaffRole {
        DOCTOR,
        NURSE,
        LAB_TECHNICIAN
    }
}
