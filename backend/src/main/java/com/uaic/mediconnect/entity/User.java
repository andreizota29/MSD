package com.uaic.mediconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name="users")
@JsonIgnoreProperties({"roles"})
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

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
