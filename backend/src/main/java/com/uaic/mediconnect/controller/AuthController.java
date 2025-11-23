package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.RegisterRequest;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.factory.PatientFactory;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.dto.LoginRequest;
import com.uaic.mediconnect.repository.UserRepo;
import com.uaic.mediconnect.security.JwtUtil;
import com.uaic.mediconnect.service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthHelperService authHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req){
        try {
            User savedUser = authService.register(req);
            return ResponseEntity.ok(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            return ResponseEntity.ok(authService.login(req));
        } catch (RuntimeException e) {
            if(e.getMessage().contains("inactive")) return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody Patient patientData, HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()) {
            return  ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            return ResponseEntity.ok(authService.completeProfile(userOpt.get(), patientData));
        } catch (Exception e ) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request){
        var userOpt = authHelper.getUserFromRequest(request);
        if(userOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Unauthorized"));
        }
        var user = userOpt.get();

        var response = Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole().name(),
                "profileCompleted", user.isProfileCompleted()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody Map<String, String> body) {
        var userOpt = authHelper.getUserFromRequest(request);

        if(userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        User user = userOpt.get();
        String newPassword = body.get("newPassword");
        if(newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Password required");
        }

        userService.changePassword(user, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = jwtUtil.generateResetToken(user.getEmail(), user.getUserId());
            String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

            try {
                emailService.sendSimpleEmail(email, "Password Reset",
                        "Click this link to reset your password: " + resetLink);
                System.out.println("Password reset email sent to: " + email);
            } catch (Exception e) {
                System.err.println("Failed to send password reset email to " + email);
                e.printStackTrace();
            }
        }


        return ResponseEntity.ok(Map.of("message", "If this email exists in our system, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Password required");
        }

        try {
            Jws<Claims> claims = jwtUtil.validateToken(token);
            Long userId = claims.getBody().get("userId", Long.class);

            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            userService.changePassword(user, newPassword);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));

        } catch (JwtException e) {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
    }

}
