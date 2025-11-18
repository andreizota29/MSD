package com.uaic.mediconnect.dto;

import com.uaic.mediconnect.entity.TimetableTemplate;
import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String title;
    private boolean active;
    private TimetableTemplate timetableTemplate;
    private UserDTO user;
    private DepartmentDTO department;
}
