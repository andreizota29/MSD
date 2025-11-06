package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.Pacient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.requests.LoginRequest;
import com.uaic.mediconnect.security.JwtUtil;
import com.uaic.mediconnect.service.PacientService;
import com.uaic.mediconnect.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PacientService pacientService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        user.setRole(Role.PATIENT);
        User savedUser = userService.addUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        var userOpt = userService.findByEmail(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        var user = userOpt.get();

        if (!userService.checkPassword(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        if (user.getRole() != Role.ADMIN && user.getRole() != loginRequest.getRole()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid role"));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getUserId());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Pacient pacientData,
                                             HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = header.substring(7);
        String email;
        try {
            Jws<Claims> claims = jwtUtil.validateToken(token);
            email = claims.getBody().getSubject();
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }

        var userOpt = userService.findByEmail(email);
        if(userOpt.isEmpty()){
            return ResponseEntity.badRequest().body("User not found");
        }

        var user = userOpt.get();
        if(!user.getRole().equals(Role.PATIENT)){
            return ResponseEntity.status(403).body("Only patients can complete a profile");
        }

        if(pacientService.findByUser(user).isPresent()){
            return ResponseEntity.status(400).body("Profile already completed");
        }

        pacientData.setUser(user);
        var saved = pacientService.addPacient(pacientData);

        return ResponseEntity.ok(saved);
    }
}
