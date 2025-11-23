package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.LoginRequest;
import com.uaic.mediconnect.dto.RegisterRequest;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.factory.PatientFactory;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.UserRepo;
import com.uaic.mediconnect.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.print.Doc;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientFactory patientFactory;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthHelperService authHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest inputUser) {
        if(userRepo.existsByEmail(inputUser.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        if(userRepo.existsByPhone(inputUser.getPhone())){
            throw new RuntimeException("Phone is already in use");
        }
        User userToSave = patientFactory.createPatientUser(inputUser);
        userService.saveWithoutEncoding(userToSave);
        return userToSave;
    }

    @Transactional
    public Map<String, Object> login(LoginRequest loginRequest){
        var userOpt = userService.findByEmail(loginRequest.getEmail());
        if(userOpt.isEmpty()){
            throw new RuntimeException("Invalid credentials");
        }
        User user = userOpt.get();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        if(user.getRole() == Role.DOCTOR){
            Doctor doctor = doctorRepo.findByUser_UserId(user.getUserId())
                    .orElseThrow(()-> new UsernameNotFoundException("Doctor account not found"));
            if(!doctor.isActive()){
                throw new RuntimeException("Doctor account is inactive");
            }
        }
        return  authHelper.generateAuthResponse(user);
    }

    @Transactional
    public Map<String, Object> completeProfile(User user, Patient patientData){
        Patient patientToCheck;
        var patientOpt = patientService.findByUser(user);

        if(patientOpt.isPresent()){
            patientToCheck = patientOpt.get();
            if (!patientToCheck.getCnp().equals(patientData.getCnp()) &&
                    patientService.cnpExists(patientData.getCnp())) {
                throw new RuntimeException("CNP is already in use by another patient");
            }
            patientToCheck.setCnp(patientData.getCnp());
            patientToCheck.setDateOfBirth(patientData.getDateOfBirth());
        } else {
            if (patientService.cnpExists(patientData.getCnp())) {
                throw new RuntimeException("CNP is already in use by another patient");
            }
            patientToCheck = patientFactory.createPatientAggregate(user, patientData);
        }

        validationService.validatePatientProfileData(patientToCheck);
        patientService.addPatient(patientToCheck);
        if(!user.isProfileCompleted()){
            user.setProfileCompleted(true);
            userService.saveWithoutEncoding(user);
        }

        String newToken = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name(), user.getUserId(), true
        );
        return Map.of("message", "Profile completed", "token", newToken);
    }
}
