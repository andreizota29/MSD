package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Role;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.security.JwtUtil;
import com.uaic.mediconnect.service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
@CrossOrigin(origins = "http://localhost:4200")
public class PatientController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PatientService patientService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthHelperService authHelper;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ClinicServiceService clinicServiceService;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request){
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()){
            return ResponseEntity.status(401).body("Invalid or unauthorized user");
        }

        var user = userOpt.get();

        var patientOpt = patientService.findByUser(user);
        if(patientOpt.isEmpty()){
            return ResponseEntity.badRequest().body("Patient not found");
        }
        var patient = patientOpt.get();
        Map<String, Object> response = Map.of(
                "CNP", patient.getCnp(),
                "dateOfBirth", patient.getDateOfBirth().toString()
        );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(HttpServletRequest request){
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()){
            return ResponseEntity.status(401).body("Invalid or unauthorized user");
        }

        var user = userOpt.get();
        var patientOpt = patientService.findByUser(user);
        patientOpt.ifPresent(patient -> patientService.deletePatient(patient));
        userService.deleteUser(user);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid or unauthorized user");
        }

        var patientOpt = patientService.findByUser(userOpt.get());
        if(patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Patient not found");
        }

        var appointments = appointmentService.findByPatient(patientOpt.get());
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(HttpServletRequest request, @RequestBody Appointment appointment) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid or unauthorized user");
        }

        var patientOpt = patientService.findByUser(userOpt.get());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Patient not found");
        }

        // Set the patient
        appointment.setPatient(patientOpt.get());

        // Set the service
        try {
            ClinicService service = clinicServiceService.getServiceById(appointment.getService().getId());
            appointment.setService(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid service ID");
        }

        // Don't set doctor or time yet; they'll be set when the patient picks them

        var savedAppointment = appointmentService.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(departmentRepo.findAll());
    }

    @GetMapping("/departments/{id}/services")
    public ResponseEntity<?> getServicesByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(clinicServiceService.findAllByDepartment(id));
    }

    //    @GetMapping("/me")
//    public ResponseEntity<?> getMyProfile(HttpServletRequest request){
//        String header = request.getHeader("Authorization");
//        if(header == null || !header.startsWith("Bearer ")) {
//            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
//        }
//        String token = header.substring(7);
//        String email;
//        try{
//            Jws<Claims> claims = jwtUtil.validateToken(token);
//            email = claims.getBody().getSubject();
//        } catch (Exception e){
//            return ResponseEntity.status(401).body("Invalid or expired token");
//        }
//
//        var userOpt = userService.findByEmail(email);
//        if(userOpt.isEmpty()){
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        var user = userOpt.get();
//        if(user.getRole() != Role.PATIENT){
//            return ResponseEntity.status(403).body("Only patients can access this endpoint");
//        }
//
//        var patientOpt = patientService.findByUser(user);
//        if(patientOpt.isEmpty()){
//            return ResponseEntity.badRequest().body("Patient not found");
//        }
//        var patient = patientOpt.get();
//        Map<String, Object> response = Map.of(
//                "insuranceNumber", patient.getInsuranceNumber(),
//                "dateOfBirth", patient.getDateOfBirth().toString(),
//                "bloodType", patient.getBloodType(),
//                "medicalHistory", patient.getMedicalHistory()
//        );
//
//        return ResponseEntity.ok(response);
//    }

    //    @DeleteMapping("/me")
//    public ResponseEntity<?> deleteMyAccount(HttpServletRequest request){
//        String header = request.getHeader("Authorization");
//        if(header == null || !header.startsWith("Bearer ")){
//            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
//        }
//
//        String token = header.substring(7);
//        String email;
//        try {
//            Jws<Claims> claims = jwtUtil.validateToken(token);
//            email = claims.getBody().getSubject();
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid or expired token");
//        }
//        var userOpt = userService.findByEmail(email);
//        if(userOpt.isEmpty()){
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        var user = userOpt.get();
//        var patientOpt = patientService.findByUser(user);
//        patientOpt.ifPresent(patient -> patientService.deletePatient(patient));
//        userService.deleteUser(user);
//        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
//    }
}
