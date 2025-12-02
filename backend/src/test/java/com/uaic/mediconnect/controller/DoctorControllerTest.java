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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DoctorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @BeforeEach
    void setUp() {
        if (userRepo.findByEmail("test-doctor@api.com").isEmpty()) {
            User doctorUser = new User("Test", "Doctor", "Parola123!", "0707222331", "test-doctor@api.com", Role.DOCTOR);
            doctorUser.setPassword(passwordEncoder.encode("Parola123!"));

            Department department = new Department();
            department.setName("Dep");
            departmentRepo.save(department);

            Doctor doctor = new Doctor();
            doctor.setTitle("Dr");
            doctor.setUser(doctorUser);
            doctor.setDepartment(department);
            doctor.setActive(true);
            doctor.setTimetableTemplate(TimetableTemplate.WEEKDAY_8_17);
            doctorRepo.save(doctor);

            User patientUser = new User("Test", "Patient", "Pass123!", "0711112111", "pat@api.com", Role.PATIENT);
            patientUser.setProfileCompleted(true);

            Patient patient = new Patient();
            patient.setUser(patientUser);
            patient.setCnp("1990101000000");
            patient.setDateOfBirth(LocalDate.now());
            patientRepo.save(patient);

            ClinicService service = new ClinicService();
            service.setName("Exam");
            service.setPrice(100.0);
            service.setDepartment(department);
            serviceRepo.save(service);

            DoctorSchedule slot = new DoctorSchedule();
            slot.setDoctor(doctor);
            slot.setDate(LocalDate.now().plusDays(1));
            slot.setStartTime(LocalTime.of(10,0));
            slot.setBooked(true);
            scheduleRepo.save(slot);

            Appointment app = new Appointment();
            app.setDoctor(doctor);
            app.setPatient(patient);
            app.setService(service);
            app.setDoctorSchedule(slot);
            app.setStatus(AppointmentStatus.SCHEDULED);
            appointmentRepo.save(app);
        }
    }

    @Test
    @DisplayName("Test Login Endpoint using RestTemplate")
    void testLoginAndGetToken() {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("test-doctor@api.com");
        loginReq.setPassword("Parola123!");

        String url = "http://localhost:" + port + "/auth/login";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, loginReq, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
    }

    @Test
    @DisplayName("Test Doctor Endpoint")
    void testGetAppointmentsProtected() {
        LoginRequest loginReq = new LoginRequest("test-doctor@api.com", "Parola123!", Role.DOCTOR);
        ResponseEntity<Map> loginResp = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", loginReq, Map.class);
        String token = (String) loginResp.getBody().get("token");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<AppointmentDTO[]> response = restTemplate.exchange(
                "http://localhost:" + port + "/doctor/appointments",
                HttpMethod.GET,
                entity,
                AppointmentDTO[].class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        AppointmentDTO[] appointments = response.getBody();
        assertThat(appointments).isNotNull();
        assertThat(appointments).isNotEmpty();
        assertThat(appointments[0].getPatient().getCnp()).isEqualTo("1990101000000");
    }

}
