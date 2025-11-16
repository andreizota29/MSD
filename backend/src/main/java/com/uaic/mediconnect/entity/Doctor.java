package com.uaic.mediconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="doctor")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointments"})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties(value = {"roles", "hibernateLazyInitializer", "handler"}, allowSetters = true)
    private User user;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"services"})
    private Department department;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "doctor_services",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ClinicService> services;

    @Enumerated(EnumType.STRING)
    @JsonProperty("timetableTemplate")
    private TimetableTemplate timetableTemplate;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DoctorSchedule> schedules;

    @Override
    public String toString() {
        return "Doctor{" +
                "title='" + title + '\'' +
                ", timetableTemplate=" + timetableTemplate +
                ", department=" + (department != null ? department.getId() : null) +
                ", user=" + (user != null ? user.getEmail() : null) +
                '}';
    }

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;

}
