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

}
