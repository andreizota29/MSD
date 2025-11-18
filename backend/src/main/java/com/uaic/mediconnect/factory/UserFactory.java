package com.uaic.mediconnect.factory;

import com.uaic.mediconnect.dto.CreateDoctorRequest;
import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Doctor createDoctorAggregate(CreateDoctorRequest req, Department department){
        User user = new User();
        user.setFirstName(req.getUserData().getFirstName());
        user.setLastName(req.getUserData().getLastName());
        user.setEmail(req.getUserData().getEmail());
        user.setPhone(req.getUserData().getPhone());
        user.setPassword(passwordEncoder.encode(req.getUserData().getPassword()));
        user.setRole(Role.DOCTOR);
        user.setProfileCompleted(true);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setTitle(req.getTitle());
        doctor.setTimetableTemplate(req.getTimetableTemplate());
        doctor.setActive(true);

        return doctor;
    }
}
