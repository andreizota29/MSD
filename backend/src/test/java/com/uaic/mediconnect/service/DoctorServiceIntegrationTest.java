package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.CreateDoctorRequest;
import com.uaic.mediconnect.dto.DepartmentDTO;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class DoctorServiceIntegrationTest {



    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private UserRepo userRepo;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("TestDep");
        departmentRepo.save(department);
    }

    @Test
    @DisplayName("Create Doctor should generate 90 days of schedule")
    void testCreateDoctorFully() {
        CreateDoctorRequest req = new CreateDoctorRequest();
        req.setTitle("Dr.");
        req.setTimetableTemplate(TimetableTemplate.WEEKDAY_8_17);

        CreateDoctorRequest.UserInput userInput = new CreateDoctorRequest.UserInput();
        userInput.setEmail("real@doctor.com");
        userInput.setPhone("0710000000");
        userInput.setFirstName("John");
        userInput.setLastName("Real");
        userInput.setPassword("Parola123!");
        req.setUserData(userInput);

        DepartmentDTO deptDTO = new DepartmentDTO();
        deptDTO.setId(department.getId());
        req.setDepartment(deptDTO);

        Doctor created = doctorService.createDoctorFully(req, "admin@test.com");

        assertThat(created.getId()).isNotNull();
        assertThat(userRepo.existsByEmail("real@doctor.com")).isTrue();

        List<DoctorSchedule> slots = scheduleRepo.findByDoctorOrderByDateAscStartTimeAsc(created);
        assertThat(slots).isNotEmpty();
        assertThat(slots.size()).isGreaterThan(100);
    }

    @Test
    @DisplayName("Create Doctor should fail if email exists")
    void testCreateDoctorDuplicateEmail() {
        User existingUser = new User("Doct", "Test", "Pass123!", "0711111111", "dup@doc.com", Role.DOCTOR);
        userRepo.save(existingUser);

        CreateDoctorRequest req = new CreateDoctorRequest();
        CreateDoctorRequest.UserInput userInput = new CreateDoctorRequest.UserInput();
        userInput.setEmail("dup@doc.com");
        userInput.setPhone("0722222222");
        userInput.setPassword("Strong123!");
        req.setUserData(userInput);

        assertThrows(RuntimeException.class, () -> {
            doctorService.createDoctorFully(req, "admin@test.com");
        });
    }


}
