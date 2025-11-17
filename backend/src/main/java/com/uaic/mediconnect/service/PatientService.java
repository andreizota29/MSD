package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.PatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    PatientRepo patientRepo;

    public void addPatient(Patient patient){
        patientRepo.save(patient);
    }

    public boolean existsByUser(User user) {
        return patientRepo.findByUser(user).isPresent();
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patientRepo.findByUser_UserId(userId);
    }

    public Optional<Patient> findByUser(User user){
        return  patientRepo.findByUser(user);
    }

    public void deletePatient(Patient patient){
        patientRepo.delete(patient);
    }

    public Optional<Patient> findByCnp(String cnp) { return patientRepo.findByCnp(cnp); }
}
