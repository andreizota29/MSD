package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
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

    @Test
    @DisplayName("Should save a patient into the database")
    void testSavePatient(){
        User user = new User();
        user.setEmail("patient@email.com");
        user.setPassword("patient123");
        user.setRole(Role.PATIENT);
        user.setFirstName("Patientus");
        user.setLastName("Pateu");
        user.setProfileCompleted(true);

        entityManager.persist(user);
        entityManager.flush();

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setCnp("1234567890123");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        Patient saved = patientRepo.save(patient);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getEmail()).isEqualTo("patient@email.com");
        assertThat(saved.getUser().isProfileCompleted()).isTrue();
        assertThat(saved.getCnp()).isEqualTo("1234567890123");
        assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("Should find patient by cnp")
    void testFindByCnp() {
        Patient patient = new Patient();
        patient.setCnp("0234567890123");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));

        User user = new User();
        user.setEmail("find@patient.com");
        user.setPassword("findpat123");
        user.setRole(Role.PATIENT);
        user.setFirstName("Petru");
        user.setLastName("Petrescu");
        user.setProfileCompleted(true);

        entityManager.persist(user);
        entityManager.flush();

        patient.setUser(user);
        entityManager.persist(patient);
        entityManager.flush();

        Optional<Patient> found = patientRepo.findByCnp("0234567890123");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getFirstName()).isEqualTo("Petru");
        assertThat(found.get().getUser().isProfileCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should return empty when CNP doesn't exist")
    void testCnpNotFound() {
        Optional<Patient> found = patientRepo.findByCnp("CEVARANDOM");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deleting an user should cascade delete the patient")
    void testCascadeDeleteUser() {
        User user = new User();
        user.setEmail("cascade@patient.com");
        user.setPassword("cascade123");
        user.setRole(Role.PATIENT);
        user.setFirstName("Cascadus");
        user.setLastName("Deleteus");
        user.setProfileCompleted(true);
        entityManager.persist(user);
        entityManager.flush();

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setCnp("9998887776665");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 5));
        entityManager.persist(patient);
        entityManager.flush();

        Optional<Patient> beforeDelete = patientRepo.findByCnp("9998887776665");
        assertThat(beforeDelete).isPresent();

        entityManager.remove(user);
        entityManager.flush();

        Optional<Patient> afterDelete = patientRepo.findByCnp("9998887776665");
        assertThat(afterDelete).isEmpty();
    }
}
