package com.uaic.mediconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name="user")
public class User {

    public User() {

    }

    public User(String lastName, String firstName, String password, String phone, String email, Role role) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.role = role;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


}
