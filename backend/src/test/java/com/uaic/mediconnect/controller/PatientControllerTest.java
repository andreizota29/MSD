package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.AppointmentDTO;
import com.uaic.mediconnect.dto.LoginRequest;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.*;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PatientControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    private String patientToken;

    @BeforeEach
    void setUp() {
        Department department = departmentRepo.findByName("Dep").orElseGet(() -> {
            Department d = new Department();
            d.setName("Dep");
            return departmentRepo.save(d);
        });

        Doctor doctor;
        if (userRepo.findByEmail("test-doctor@api.com").isEmpty()) {
            User doctorUser = new User("Test", "Doctor", "Parola123!", "0707222331", "test-doctor@api.com", Role.DOCTOR);
            doctorUser.setPassword(passwordEncoder.encode("Parola123!"));

            doctor = new Doctor();
            doctor.setTitle("Dr");
            doctor.setUser(doctorUser);
            doctor.setDepartment(department);
            doctor.setActive(true);
            doctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_8_17);
            doctor = doctorRepo.save(doctor);
        } else {
            doctor = doctorRepo.findByUserEmail("test-doctor@api.com").orElseThrow();
        }

        Patient patient;
        if (userRepo.findByEmail("test-patient@api.com").isEmpty()) {
            User patientUser = new User("Test", "Patient", "Pass123!", "0711112111", "test-patient@api.com", Role.PATIENT);
            patientUser.setProfileCompleted(true);
            patientUser.setPassword(passwordEncoder.encode("Pass123!"));

            patient = new Patient();
            patient.setUser(patientUser);
            patient.setCnp("1990101000000");
            patient.setDateOfBirth(LocalDate.now());
            patient = patientRepo.save(patient);
        } else {
            patient = patientRepo.findByUser(userRepo.findByEmail("test-patient@api.com").get()).orElseThrow();
        }

        ClinicService service;
        var services = serviceRepo.findByDepartment(department);
        if (services.isEmpty()) {
            service = new ClinicService();
            service.setName("Exam");
            service.setPrice(100.0);
            service.setDepartment(department);
            service = serviceRepo.save(service);
        } else {
            service = services.get(0);
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DoctorSchedule slot;
        var slots = scheduleRepo.findByDoctorAndDateOrderByStartTimeAsc(doctor, tomorrow);

        if (slots.isEmpty()) {
            slot = new DoctorSchedule();
            slot.setDoctor(doctor);
            slot.setDate(tomorrow);
            slot.setStartTime(LocalTime.of(10, 0));
            slot.setBooked(true);
            slot = scheduleRepo.save(slot);
        } else {
            slot = slots.get(0);
            if (!slot.isBooked()) {
                slot.setBooked(true);
                scheduleRepo.save(slot);
            }
        }

        final DoctorSchedule finalSlot = slot;
        if (appointmentRepo.findAll().stream().noneMatch(a -> a.getDoctorSchedule().getId().equals(finalSlot.getId()))) {
            Appointment app = new Appointment();
            app.setDoctor(doctor);
            app.setPatient(patient);
            app.setService(service);
            app.setDoctorSchedule(slot);
            app.setStatus(AppointmentStatus.SCHEDULED);
            appointmentRepo.save(app);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test-patient@api.com");
        loginReq.setPassword("Pass123!");

        String url = "http://localhost:" + port + "/auth/login";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, loginReq, Map.class);
        this.patientToken = (String) response.getBody().get("token");
    }

    @Test
    @DisplayName("Test Access Denied for Incomplete Profile")
    void testAccessWithIncompleteProfile() {
        if (userRepo.findByEmail("incomplete@test.com").isEmpty()) {
            User incompleteUser = new User("Incomp", "User", "Pass123!", "0720223321", "inc@test.com", Role.PATIENT);
            incompleteUser.setPassword(passwordEncoder.encode("Pass123!"));
            incompleteUser.setProfileCompleted(false);
            userRepo.save(incompleteUser);
        }

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("inc@test.com");
        loginReq.setPassword("Pass123!");
        ResponseEntity<Map> loginResp = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", loginReq, Map.class);
        String token = (String) loginResp.getBody().get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/patient/me",
                HttpMethod.GET,
                entity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /patient/appointments")
    void testGetAppointmentsProtected() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(patientToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<AppointmentDTO[]> response = restTemplate.exchange(
                "http://localhost:" + port + "/patient/appointments/list",
                HttpMethod.GET,
                entity,
                AppointmentDTO[].class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AppointmentDTO[] appointments = response.getBody();
        assertThat(appointments).isNotNull();
        assertThat(appointments).isNotEmpty();
        assertThat(appointments[0].getPatient().getCnp()).isEqualTo("1990101000000");

        ResponseEntity<String> responseDelete = restTemplate.exchange(
                "http://localhost:" + port + "/patient/appointments/" + appointments[0].getId(),
                HttpMethod.DELETE,
                entity,
                String.class
        );
        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("DELETE /patient/me")
    void testDeleteAccount() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(patientToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/patient/me",
                HttpMethod.DELETE,
                entity,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userRepo.findByEmail("test-patient@api.com")).isEmpty();
    }

}