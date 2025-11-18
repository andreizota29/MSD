package com.uaic.mediconnect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;

    @Pattern(regexp = "^(\\+40|0)7\\d{8}$", message = "Invalid Romanian phone number. Use format 07xxxxxxxx or +407xxxxxxxx")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 chars, contain 1 uppercase, 1 lowercase, 1 digit, and 1 special char")
    private String password;
}
