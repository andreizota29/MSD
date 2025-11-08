package com.uaic.mediconnect.entity;

import jakarta.persistence.*;


@Entity
@Table(name="User")
public class User {

    public User() {

    }

    public User(String lastName, String firstName, String password, String phone, String email, Role role, StaffType staffType) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.staffType = role == Role.STAFF ? staffType : null;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column
    private String phone;

    @Column(unique = true, nullable = false)
    private String password;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(nullable = false)
    private boolean profileCompleted = false;

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    @Column
    @Enumerated(EnumType.STRING)
    private StaffType staffType; // null for PATIENT users

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public StaffType getStaffType() {
        return staffType;
    }

    public void setStaffType(StaffType staffType) {
        this.staffType = staffType;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isStaff() {
        return role == Role.STAFF;
    }

    public StaffType getStaffTypeSafe() {
        return isStaff() ? staffType : null;
    }
}
