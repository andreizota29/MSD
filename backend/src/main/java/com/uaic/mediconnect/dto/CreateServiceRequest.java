package com.uaic.mediconnect.dto;

import lombok.Data;

@Data
public class CreateServiceRequest {
    private String name;
    private Double price;
    private DepartmentDTO department;
}
