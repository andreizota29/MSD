package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.UserRepo;
import com.uaic.mediconnect.requests.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User addUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public  boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Boolean loginUser(LoginRequest loginRequest) {
        Optional<User> user = userRepo.findByEmail(loginRequest.getEmail());
        if(user == null) {
            return false;
        }

        User user1 = user.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user1.getPassword())) {
            return false;
        }

        if (user1.getRole() != loginRequest.getRole()) {
            return false;
        }

        return true;

    }

    public User saveWithoutEncoding(User user) {
        return userRepo.save(user);
    }

    public void deleteUser(User user){
        userRepo.delete(user);
    }
}
