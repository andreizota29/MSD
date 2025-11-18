package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Department;
import org.junit.jupiter.api.BeforeEach;
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

    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testDepartment = new Department();
        testDepartment.setName("Test Dept");
        entityManager.persist(testDepartment);
        entityManager.flush();
    }

    @Test
    void testSaveService() {
        ClinicService service = new ClinicService();
        service.setDepartment(testDepartment);
        service.setName("Checkup");
        service.setPrice(150.00);

        ClinicService saved = clinicServiceRepo.save(service);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Checkup");
        assertThat(saved.getDepartment().getName()).isEqualTo("Test Dept");
    }
}
