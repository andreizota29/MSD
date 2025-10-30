package com.uaic.mediconnect.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="User")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String firstName;
    private String lastName;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Boolean isActive = true;

    public enum Role {
        PATIENT,
        STAFF,
        ADMIN
    }
}
