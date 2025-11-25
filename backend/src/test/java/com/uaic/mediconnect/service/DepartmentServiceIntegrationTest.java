package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class DepartmentServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClinicServiceRepo clinicServiceRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private UserRepo userRepo;

    @Test
    void testDeleteDepartmentFully() {
        Department dept = new Department();
        dept.setName("To Delete");
        departmentRepo.save(dept);

        User docUser = new User("Doc", "InDept", "Pass!", "0702222000", "doc@dept.com", Role.DOCTOR);
        userRepo.save(docUser);
        Doctor doctor = new Doctor();
        doctor.setUser(docUser);
        doctor.setDepartment(dept);
        doctor.setTitle("Dr");
        doctorRepo.save(doctor);
        ClinicService service = new ClinicService();
        service.setName("Service In Dept");
        service.setPrice(10.0);
        service.setDepartment(dept);
        clinicServiceRepo.save(service);

        departmentService.deleteDepartmentFully(dept.getId(), "admin@test.com");

        assertThat(departmentRepo.findById(dept.getId())).isEmpty();
        assertThat(doctorRepo.findById(doctor.getId())).isEmpty();
        assertThat(clinicServiceRepo.findById(service.getId())).isEmpty();
    }
}
