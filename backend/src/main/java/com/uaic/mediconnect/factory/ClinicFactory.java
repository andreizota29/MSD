package com.uaic.mediconnect.factory;

import com.uaic.mediconnect.dto.CreateServiceRequest;
import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class ClinicFactory {
    public Department createDepartment(String name) {
        Department dept = new Department();
        dept.setName(name);
        return dept;
    }

    public ClinicService createService(CreateServiceRequest req, Department department) {
        ClinicService service = new ClinicService();
        service.setName(req.getName());
        service.setPrice(req.getPrice());
        service.setDepartment(department);
        return service;
    }
}
