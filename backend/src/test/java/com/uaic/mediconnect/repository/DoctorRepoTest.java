package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DoctorRepoTest {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save a doctor into the database")
    void testSaveDoctor() {
        User user = new User();
        user.setEmail("doctor@email.com");
        user.setPassword("doctor123");
        user.setRole(Role.DOCTOR);
        user.setFirstName("Doctor");
        user.setLastName("Doctorescu");
        user.setProfileCompleted(false);

        entityManager.persist(user);
        entityManager.flush();

        Department department = new Department();
        department.setName("DepartamentTest");
        entityManager.persist(department);
        entityManager.flush();

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setTitle("Doctor Test");
        doctor.setActive(true);
        Doctor saved = doctorRepo.save(doctor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getEmail()).isEqualTo("doctor@email.com");
        assertThat(saved.getDepartment().getName()).isEqualTo("DepartamentTest");
    }

//    @Test
//    @DisplayName("Deleting a department should cascade delete the doctor and user")
//    void testCascadeDeleteDepartment(){
//        Department department = new Department();
//        department.setName("DepTest");
//        department = entityManager.persistAndFlush(department);
//
//        User user = new User();
//        user.setEmail("cascade2@depart.com");
//        user.setPassword("passcascade");
//        user.setRole(Role.DOCTOR);
//        user.setProfileCompleted(false);
//        user.setFirstName("CasTest");
//        user.setLastName("DocTest");
//        user = entityManager.persistAndFlush(user);
//
//        Doctor doctor = new Doctor();
//        doctor.setTitle("Doctor CasTest");
//        doctor.setActive(true);
//        doctor.setDepartment(department);
//        doctor.setUser(user);
//        doctor = entityManager.persistAndFlush(doctor);
//
//        Optional<Doctor> beforeDeleteDoctor = doctorRepo.findByUserEmail("cascade2@depart.com");
//        assertThat(beforeDeleteDoctor).isPresent();
//
//        Optional<Department> beforeDeleteDepartment = departmentRepo.findByName("DepTest");
//        assertThat(beforeDeleteDepartment).isPresent();
//
//        entityManager.remove(department);
//        entityManager.flush();
//
//        Optional<Doctor> afterDeleteDoctor = doctorRepo.findByUserEmail("cascade@depart.com");
//        assertThat(afterDeleteDoctor).isEmpty();
//
//    }

}
