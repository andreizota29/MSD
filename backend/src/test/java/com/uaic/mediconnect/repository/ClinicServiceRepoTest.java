package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
public class ClinicServiceRepoTest {

    @Autowired
    private ClinicServiceRepo clinicServiceRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save a service into the database")
    void testSaveService() {
        Department department = new Department();
        department.setName("DeparTest");
        entityManager.persist(department);
        entityManager.flush();

        ClinicService clinicService = new ClinicService();
        clinicService.setDepartment(department);
        clinicService.setName("Serviciu");
        clinicService.setPrice(100.00);
        ClinicService saved = clinicServiceRepo.save(clinicService);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Serviciu");
        assertThat(saved.getDepartment().getName()).isEqualTo("DeparTest");
    }
}
