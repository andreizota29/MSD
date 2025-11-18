package com.uaic.mediconnect.dto;

import lombok.Data;

@Data
public class ClinicServiceDTO {
    private Long id;
    private String name;
    private Double price;
    private DepartmentDTO department;
}
