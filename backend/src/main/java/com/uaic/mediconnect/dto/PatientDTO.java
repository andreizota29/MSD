package com.uaic.mediconnect.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientDTO {
    private Long id;
    private String cnp;
    private LocalDate dateOfBirth;
    private UserDTO user;
}
