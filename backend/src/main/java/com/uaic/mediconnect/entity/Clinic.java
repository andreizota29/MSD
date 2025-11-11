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

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public List<DepartmentType> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentType> departments) {
        this.departments = departments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }
}
