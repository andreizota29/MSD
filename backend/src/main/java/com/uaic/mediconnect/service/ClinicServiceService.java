package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicServiceService {

    @Autowired
    private ClinicServiceRepo serviceRepo;

    public ClinicService getServiceById(Long id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service ID"));
    }

    public List<ClinicService> findAllByDepartment(Long departmentId) {
        return serviceRepo.findAll().stream()
                .filter(s -> s.getDepartment().getId().equals(departmentId))
                .toList();
    }
}