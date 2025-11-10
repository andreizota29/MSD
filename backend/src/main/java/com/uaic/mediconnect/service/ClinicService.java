package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Clinic;
import com.uaic.mediconnect.repository.ClinicRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicService {

    @Autowired
    private ClinicRepo clinicRepo;

    public List<Clinic> getAllClinics(){
        return clinicRepo.findAll();
    }

    public Clinic addClinic(Clinic clinic) {
        return clinicRepo.save(clinic);
    }
}
