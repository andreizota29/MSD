package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

@DataJpaTest
public class PatientRepoTest {

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private TestEntityManager entityManager;

    private User patientUser;

    @BeforeEach
    void setUp() {
        patientUser = new User("Pat", "Ient", "pass", "0733222662", "patient@test.com", Role.PATIENT);
        patientUser.setProfileCompleted(true);
        entityManager.persist(patientUser);
        entityManager.flush();
    }

    @Test
    void testSavePatient() {
        Patient patient = new Patient();
        patient.setUser(patientUser);
        patient.setCnp("1990000000000");
        patient.setDateOfBirth(LocalDate.of(1999, 1, 1));

        Patient saved = patientRepo.save(patient);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getEmail()).isEqualTo("patient@test.com");
    }

    @Test
    void testFindByCnp() {
        Patient patient = new Patient();
        patient.setUser(patientUser);
        patient.setCnp("2990000000000");
        patient.setDateOfBirth(LocalDate.of(1999, 1, 1));
        entityManager.persist(patient);
        entityManager.flush();

        Optional<Patient> found = patientRepo.findByCnp("2990000000000");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getLastName()).isEqualTo("Pat");
    }

    @Test
    @DisplayName("Should return empty when CNP doesn't exist")
    void testCnpNotFound() {
        Optional<Patient> found = patientRepo.findByCnp("CEVARANDOM");
        assertThat(found).isEmpty();
    }

}
