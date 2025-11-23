package com.uaic.mediconnect.factory;

import com.uaic.mediconnect.dto.RegisterRequest;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PatientFactory {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createPatientUser(RegisterRequest req) {
        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.PATIENT);
        user.setProfileCompleted(false);
        return user;
    }

    public Patient createPatientAggregate(User user, Patient inputData) {
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setCnp(inputData.getCnp());
        patient.setDateOfBirth(inputData.getDateOfBirth());
        return patient;
    }
}
