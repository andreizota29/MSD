package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.Clinic;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.service.ClinicService;
import com.uaic.mediconnect.service.UserService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClinicService clinicService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/clinics")
    public ResponseEntity<?> getAllClinics(){
        return ResponseEntity.ok(clinicService.getAllClinics());
    }

    @PostMapping("/clinics")
    public ResponseEntity<?> createClinic(@RequestBody Clinic clinic){
        return ResponseEntity.ok(clinicService.addClinic(clinic));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user){
        return ResponseEntity.ok(userService.addUser(user));
    }

    @PutMapping("/clinics/{id}")
    public ResponseEntity<?> updateClinic(@PathVariable Long id, @RequestBody Clinic clinic){
        return ResponseEntity.ok(clinicService.updateClinic(id, clinic));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user){
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/clinics/{id}")
    public ResponseEntity<?> deleteClinic(@PathVariable Long id){
        clinicService.deleteClinicById(id);
        return ResponseEntity.ok(Map.of("message","Clinic deleted successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message","User deleted successfully"));
    }
}
