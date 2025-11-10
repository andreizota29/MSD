package com.uaic.mediconnect.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="staff")
public abstract class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long staffId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentType department;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private boolean isOnDuty;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffSchedule> schedules;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffScheduleException> scheduleExceptions;

    public List<StaffSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<StaffSchedule> schedules) {
        this.schedules = schedules;
    }

    public List<StaffScheduleException> getScheduleExceptions() {
        return scheduleExceptions;
    }

    public void setScheduleExceptions(List<StaffScheduleException> scheduleExceptions) {
        this.scheduleExceptions = scheduleExceptions;
    }
//    @ManyToMany
//    @JoinTable(
//            name = "doctor_clinics",
//            joinColumns = @JoinColumn(name="doctor_id"),
//            inverseJoinColumns = @JoinColumn(name="clinic_id")
//    )
//    private List<Clinic> clinics;
//
//    public List<Clinic> getClinics() {
//        return clinics;
//    }
//
//    public void setClinics(List<Clinic> clinics) {
//        this.clinics = clinics;
//    }

    public DepartmentType getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentType department) {
        this.department = department;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isOnDuty() {
        return isOnDuty;
    }

    public void setOnDuty(boolean onDuty) {
        isOnDuty = onDuty;
    }
}
