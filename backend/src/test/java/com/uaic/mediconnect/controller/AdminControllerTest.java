package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.LoginRequest;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private String adminToken;

    @BeforeEach
    void setUp() {
        if(userRepo.findByEmail("test-admin@api.com").isEmpty()){
            User admin = new User();
            admin.setEmail("test-admin@api.com");
            admin.setFirstName("Test");
            admin.setLastName("Admin");
            admin.setPhone("0755070000");
            admin.setRole(Role.ADMIN);
            admin.setProfileCompleted(true);
            admin.setPassword(passwordEncoder.encode("admin123"));

            userRepo.save(admin);
        }
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test-admin@api.com");
        loginReq.setPassword("admin123");

        String loginUrl = "http://localhost:" + port + "/auth/login";
        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, loginReq, Map.class);
        this.adminToken = (String) response.getBody().get("token");
    }

    @Test
    @DisplayName("GET /admin/departments")
    void testGetDepartmentsProtected() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/admin/departments",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /admin/departments")
    void testCreateDepartment() {
        Department dept = new Department();
        dept.setName("New Integration Dept");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Department> entity = new HttpEntity<>(dept, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/admin/departments",
                HttpMethod.POST,
                entity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("DELETE /admin/departments/{id}")
    void testDeleteDepartment() {
        Department dept = new Department();
        dept.setName("Delete Me");
        Department saved = departmentRepo.save(dept);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/admin/departments/" + saved.getId(),
                HttpMethod.DELETE,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(departmentRepo.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("PUT /admin/doctors/{id} - Should return 200 OK")
    void testUpdateDoctor() {
        User docUser = new User("Doc", "Old", "pass", "0799000999", "update@doc.com", Role.DOCTOR);
        Department dept = new Department();
        dept.setName("Update Dept");
        departmentRepo.save(dept);

        Doctor doctor = new Doctor();
        doctor.setUser(docUser);
        doctor.setDepartment(dept);
        doctor.setTitle("Dr");
        doctor.setActive(true);
        doctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_9_18);
        Doctor savedDoc = doctorRepo.save(doctor);

        Doctor updatePayload = new Doctor();
        updatePayload.setTitle("Prof. Dr.");
        updatePayload.setTimetableTemplate(TimetableTemplate.WEEKDAY_9_18);

        User userUpdate = new User();
        userUpdate.setFirstName("Doc");
        userUpdate.setLastName("NewName");
        userUpdate.setPhone("0799000999");
        userUpdate.setEmail("update@doc.com");
        updatePayload.setUser(userUpdate);

        updatePayload.setDepartment(dept);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Doctor> entity = new HttpEntity<>(updatePayload, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/admin/doctors/" + savedDoc.getId(),
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Doctor updatedInDb = doctorRepo.findById(savedDoc.getId()).get();
        assertThat(updatedInDb.getTitle()).isEqualTo("Prof. Dr.");
        assertThat(updatedInDb.getUser().getLastName()).isEqualTo("NewName");
    }
}
