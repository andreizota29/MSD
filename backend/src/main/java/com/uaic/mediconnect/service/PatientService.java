package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.PatientRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    PatientRepo patientRepo;

    public Patient addPatient(Patient patient){
        return patientRepo.save(patient);
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patientRepo.findByUserId(userId);
    }

    public Optional<Patient> findByUser(User user){
        return  patientRepo.findByUser(user);
    }


}
