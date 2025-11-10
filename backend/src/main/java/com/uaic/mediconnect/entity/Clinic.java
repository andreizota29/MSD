package com.uaic.mediconnect.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="clinic")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clinicId;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<DepartmentType> departments;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @ManyToMany(mappedBy = "clinics")
    private List<Doctor> doctors;

}
