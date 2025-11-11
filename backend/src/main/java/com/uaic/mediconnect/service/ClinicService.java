package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Clinic;
import com.uaic.mediconnect.repository.ClinicRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Clinic> getClinicById(Long id){
        return clinicRepo.findById(id);
    }

    public void deleteClinicById(Long id){
        clinicRepo.deleteById(id);
    }

    public Clinic updateClinic(Long id, Clinic updatedClinic) {
        return clinicRepo.findById(id)
                .map(clinic -> {
                    clinic.setName(updatedClinic.getName());
                    clinic.setAddress(updatedClinic.getAddress());
                    clinic.setDepartments(updatedClinic.getDepartments());
                    return clinicRepo.save(clinic);
                })
                .orElseThrow(() -> new RuntimeException("Clinic not found"));
    }

}
