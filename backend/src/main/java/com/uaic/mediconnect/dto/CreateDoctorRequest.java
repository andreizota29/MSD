package com.uaic.mediconnect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uaic.mediconnect.entity.TimetableTemplate;
import lombok.Data;

@Data
public class CreateDoctorRequest {

    @JsonProperty("user")
    private UserInput userData;

    private String title;
    private DepartmentDTO department;
    private TimetableTemplate timetableTemplate;

    @Data
    public static class UserInput {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String password;
    }

    private UserInput userInput;
}
