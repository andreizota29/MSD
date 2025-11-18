package com.uaic.mediconnect.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private boolean profileCompleted;
}
