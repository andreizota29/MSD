package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.RegisterRequest;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.PatientRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class AuthServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Registering with duplicate email should fail")
    void testRegisterDuplicate() {
        User u = new User("Old", "User", "Pass123!", "0722111111", "dup@test.com", Role.PATIENT);
        userRepo.save(u);

        RegisterRequest req = new RegisterRequest();
        req.setEmail("dup@test.com");
        req.setPhone("0712321123");
        req.setFirstName("Morti");
        req.setLastName("Raniti");
        req.setPassword("Pass123!");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(req));
        assertThat(ex.getMessage()).contains("Email is already in use");
    }

    @Test
    @DisplayName("Completing profile with used CNP should fail")
    void testCompleteProfileDuplicateCNP() {
        User u1 = new User("One", "U", "Pass!", "0700111411", "one@test.com", Role.PATIENT);
        u1.setProfileCompleted(true);
        userRepo.save(u1);
        Patient p1 = new Patient();
        p1.setUser(u1);
        p1.setCnp("1720603412707");
        p1.setDateOfBirth(LocalDate.of(1972,6,3));
        patientRepo.save(p1);

        User u2 = new User("Two", "U", "Pass!", "0700222222", "two@test.com", Role.PATIENT);
        userRepo.save(u2);

        Patient inputData = new Patient();
        inputData.setCnp("1720603412707");
        inputData.setDateOfBirth(LocalDate.of(1972,6,3));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.completeProfile(u2, inputData));
        assertThat(ex.getMessage()).contains("CNP is already in use");
    }
}
