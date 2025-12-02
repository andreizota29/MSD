package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.LoginRequest;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private String generateValidPhone() {
        return "07" + (10000000 + new Random().nextInt(89999999));
    }

    @Test
    @DisplayName("POST /auth/register - Should create new user")
    void testRegisterSuccess() {
        String email = "new-reg-" + new Random().nextInt(10000000) + "@test.com";
        String phone = generateValidPhone();

        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setPassword("StrongPass1!");
        req.setFirstName("New");
        req.setLastName("User");
        req.setPhone(phone);

        ResponseEntity<User> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/register",
                req,
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(email);

        assertThat(userRepo.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("POST /auth/login")
    void testLoginSuccess() {
        String email = "login-test@api.com";
        if (userRepo.findByEmail(email).isEmpty()) {
            User user = new User("Test", "Login", "StrongPass1!", "0799999999", email, Role.PATIENT);
            user.setPassword(passwordEncoder.encode("StrongPass1!"));
            user.setProfileCompleted(true);
            userRepo.save(user);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword("StrongPass1!");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/login",
                loginReq,
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody()).containsKey("profileCompleted");
    }

    @Test
    @DisplayName(" Register Login Complete Profile")
    void testCompleteProfileFlow() {
        String email = "flow-" + new Random().nextInt(10000000) + "@test.com";
        String phone = generateValidPhone();

        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail(email);
        regReq.setPassword("StrongPass1!");
        regReq.setFirstName("Flow");
        regReq.setLastName("User");
        regReq.setPhone(phone);

        ResponseEntity<User> regRes = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/register",
                regReq,
                User.class
        );

        assertThat(regRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword("StrongPass1!");

        ResponseEntity<Map> loginRes = restTemplate.postForEntity(
                "http://localhost:" + port + "/auth/login",
                loginReq,
                Map.class
        );

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) loginRes.getBody().get("token");

        assertThat(loginRes.getBody().get("profileCompleted")).isEqualTo(false);

        Patient patientData = new Patient();
        patientData.setCnp("1990101010015");
        patientData.setDateOfBirth(LocalDate.of(1999, 1, 1));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Patient> entity = new HttpEntity<>(patientData, headers);

        ResponseEntity<Map> profileRes = restTemplate.exchange(
                "http://localhost:" + port + "/auth/complete-profile",
                HttpMethod.POST,
                entity,
                Map.class
        );

        assertThat(profileRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileRes.getBody()).containsKey("token");

        User user = userRepo.findByEmail(email).get();
        assertThat(user.isProfileCompleted()).isTrue();
        assertThat(patientRepo.findByUser(user)).isPresent();
    }

    @Test
    @DisplayName("PUT /auth/change-password")
    void testChangePassword() {
        String email = "changepass-" + new Random().nextInt(10000000) + "@test.com";
        User user = new User("Chg", "Pass", "OldPass1!", generateValidPhone(), email, Role.PATIENT);
        user.setPassword(passwordEncoder.encode("OldPass1!"));
        user.setProfileCompleted(true);
        userRepo.save(user);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(email);
        loginReq.setPassword("OldPass1!");
        ResponseEntity<Map> loginRes = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", loginReq, Map.class);
        String token = (String) loginRes.getBody().get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        Map<String, String> body = Map.of("newPassword", "NewPass1!");
        HttpEntity<Map> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> changeRes = restTemplate.exchange(
                "http://localhost:" + port + "/auth/change-password",
                HttpMethod.PUT,
                entity,
                Map.class
        );

        assertThat(changeRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        loginReq.setPassword("NewPass1!");
        ResponseEntity<Map> newLoginRes = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", loginReq, Map.class);
        assertThat(newLoginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}