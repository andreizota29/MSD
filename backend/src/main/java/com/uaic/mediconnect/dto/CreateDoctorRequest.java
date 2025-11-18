package com.uaic.mediconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uaic.mediconnect.entity.TimetableTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateDoctorRequest {

    @JsonProperty("user")
    @Valid
    private UserInput userData;

    private String title;
    private DepartmentDTO department;
    private TimetableTemplate timetableTemplate;

    @Data
    public static class UserInput {
        private String firstName;
        private String lastName;

        @Email(message = "Invalid email format")
        private String email;

        @Pattern(regexp = "^(\\+40|0)7\\d{8}$", message = "Invalid Romanian phone number")
        private String phone;

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be 8+ chars, with 1 Upper, 1 Lower, 1 Digit, 1 Special")
        private String password;
    }

    private UserInput userInput;
}
