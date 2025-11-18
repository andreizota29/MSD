package com.uaic.mediconnect.repository;

import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

    private User baseUser;

    @BeforeEach
    void setUp() {
        baseUser = new User();
        baseUser.setEmail("base@test.com");
        baseUser.setPassword("Parola123!");
        baseUser.setRole(Role.PATIENT);
        baseUser.setFirstName("Base");
        baseUser.setLastName("User");
        baseUser.setProfileCompleted(true);
        entityManager.persist(baseUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save a new user")
    void testSaveUser() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setPassword("Password123!");
        newUser.setRole(Role.DOCTOR);
        newUser.setFirstName("New");
        newUser.setLastName("User");

        User saved = userRepo.save(newUser);

        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        Optional<User> found = userRepo.findByEmail("base@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Base");
    }

    @Test
    void testFindEmailNotFound(){
        Optional<User> found = userRepo.findByEmail("ghost@test.com");
        assertThat(found).isEmpty();
    }

}
