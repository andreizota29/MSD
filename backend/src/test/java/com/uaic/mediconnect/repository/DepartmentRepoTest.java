package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DepartmentRepoTest {

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;


    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save a department into the database")
    void testSaveDepartment(){
        Department department = new Department();
        department.setName("Dep");

        Department saved = departmentRepo.save(department);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dep");
    }

    @Test
    @DisplayName("Deleting a department should cascade delete the doctor and user")
    void testCascadeDeleteDepartment(){
        User user = new User();
        user.setEmail("cascade@doctor.com");
        user.setPassword("cascadeD123");
        user.setRole(Role.DOCTOR);
        user.setProfileCompleted(false);
        user.setFirstName("Cascadus");
        user.setLastName("Doctorus");
        entityManager.persist(user);
        entityManager.flush();

        Department department = new Department();
        department.setName("DepOne");
        entityManager.persist(department);
        entityManager.flush();

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setTitle("Doctor Cascade");
        doctor.setActive(true);
        entityManager.persist(doctor);
        entityManager.flush();

        Optional<Doctor> beforeDeleteDoctor = doctorRepo.findByUserEmail("cascade@doctor.com");
        assertThat(beforeDeleteDoctor).isPresent();

        Optional<Department> beforeDeleteDepartment = departmentRepo.findByName("DepOne");
        assertThat(beforeDeleteDepartment).isPresent();

        entityManager.remove(department);
        entityManager.flush();

        Optional<Doctor> afterDeleteDoctor = doctorRepo.findByUserEmail("cascade@doctor.com");
        assertThat(afterDeleteDoctor).isEmpty();





    }
}
