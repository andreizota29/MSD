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
public class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DoctorRepo doctorRepo;

    @Test
    @DisplayName("Should save an user to the database")
    void testSaveUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setPassword("parola123");
        user.setRole(Role.PATIENT);
        user.setFirstName("Testo");
        user.setLastName("Putesto");

        User saved = userRepo.save(user);

        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        User user = new User();
        user.setEmail("find@email.com");
        user.setPassword("finder123");
        user.setRole(Role.DOCTOR);
        user.setFirstName("Doctorescu");
        user.setLastName("Findescu");

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepo.findByEmail("find@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.DOCTOR);
        assertThat(found.get().getFirstName()).isEqualTo("Doctorescu");
    }

    @Test
    @DisplayName("Should return empty when email doesn't exist")
    void testFindEmailNotFound(){
        Optional<User> found = userRepo.findByEmail("nimic@zero.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deleting a doctor should cascade delete the user")
    void testCascadeDeleteDepartment(){
        Department department = new Department();
        department.setName("DepOne2");
        entityManager.persist(department);

        User user = new User();
        user.setEmail("cascade@depart.com");
        user.setPassword("cascadeD1232");
        user.setRole(Role.DOCTOR);
        user.setProfileCompleted(false);
        user.setFirstName("Cascadus2");
        user.setLastName("Doctorus2");
        entityManager.persist(user);

        Doctor doctor = new Doctor();
        doctor.setTitle("Doctor Cascade2");
        doctor.setActive(true);
        doctor.setDepartment(department);
        doctor.setUser(user);
        entityManager.persist(doctor);
        entityManager.flush();

        Optional<User> beforeDelete = userRepo.findByEmail("cascade@depart.com");
        assertThat(beforeDelete).isPresent();

        entityManager.remove(doctor);
        entityManager.flush();

        Optional<User> afterDelete = userRepo.findByEmail("cascade@depart.com");
        assertThat(userRepo.findByEmail("cascade@depart.com")).isEmpty();
        assertThat(doctorRepo.findById(doctor.getId())).isEmpty();

    }
}
