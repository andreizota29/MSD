package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.security.JwtUtil;
import com.uaic.mediconnect.service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private EmailService emailService;


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

    @GetMapping("/appointments/list")
    public ResponseEntity<?> getMyAppointmentsList(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        var patientOpt = patientService.findByUser(userOpt.get());
        if (patientOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Patient not found");
        }

        var appointments = appointmentService.findByPatient(patientOpt.get())
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.COMPLETED)
                .toList();

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

        appointment.setPatient(patientOpt.get());

        try {
            ClinicService service = clinicServiceService.getServiceById(appointment.getService().getId());
            appointment.setService(service);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid service ID");
        }

        var savedAppointment = appointmentService.save(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAppointment);
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> cancelAppointment(HttpServletRequest request, @PathVariable Long id) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        var patientOpt = patientService.findByUser(userOpt.get());
        if (patientOpt.isEmpty()) return ResponseEntity.badRequest().body("Patient not found");

        var appointmentOpt = appointmentService.findById(id);
        if (appointmentOpt.isEmpty()) return ResponseEntity.badRequest().body("Appointment not found");

        var appointment = appointmentOpt.get();

        if (!appointment.getPatient().getId().equals(patientOpt.get().getId()))
            return ResponseEntity.status(403).body("Cannot cancel this appointment");

        if (appointment.getStatus() == AppointmentStatus.COMPLETED)
            return ResponseEntity.badRequest().body("Cannot cancel completed appointments");

        appointmentService.cancelAppointment(appointment);

        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments() {
        return ResponseEntity.ok(departmentRepo.findAll());
    }

    @GetMapping("/departments/{id}/services")
    public ResponseEntity<?> getServicesByDepartment(@PathVariable Long id) {
        var services = clinicServiceService.findAllByDepartment(id);
        services.forEach(s -> {
            Department dept = s.getDepartment();
            if (dept != null) {
                Department d = new Department();
                d.setId(dept.getId());
                d.setName(dept.getName());
                s.setDepartment(d);
            }
            s.setAppointments(null);
        });
        return ResponseEntity.ok(services);
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(
            HttpServletRequest request,
            @RequestParam Long slotId,
            @RequestParam Long serviceId
    ) {

        var user = authHelper.getPatientUserFromRequest(request);
        if (user.isEmpty())
            return ResponseEntity.status(401).body("Unauthorized");

        var patient = patientService.findByUser(user.get())
                .orElse(null);

        if (patient == null)
            return ResponseEntity.badRequest().body("Patient not found");

        ClinicService service = clinicServiceService.getServiceById(serviceId);

        DoctorSchedule slot = scheduleRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.isBooked())
            return ResponseEntity.status(400).body("Slot already booked");

        slot.setBooked(true);
        slot.setPatient(patient);
        scheduleRepo.save(slot);

        Appointment appointment = new Appointment();
        appointment.setDoctor(slot.getDoctor());
        appointment.setPatient(patient);
        appointment.setService(service);
        appointment.setDoctorSchedule(slot);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        appointmentService.save(appointment);

        try {
            String to = patient.getUser().getEmail();
            String subject = "Appointment Confirmation";
            String text = String.format(
                    "Hello %s %s,\n\nYour appointment for '%s' with Dr. %s %s on %s at %s has been successfully booked.\n\nThank you!",
                    patient.getUser().getFirstName(),
                    patient.getUser().getLastName(),
                    service.getName(),
                    slot.getDoctor().getUser().getFirstName(),
                    slot.getDoctor().getUser().getLastName(),
                    slot.getDate().toString(),
                    slot.getStartTime()
            );
            emailService.sendSimpleEmail(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Appointment booked successfully",
                "appointmentId", appointment.getId()
        ));
    }



    @GetMapping("/departments/{depId}/services/{serviceId}/slots")
    public ResponseEntity<?> getAvailableSlots(
            HttpServletRequest request,
            @PathVariable Long depId,
            @PathVariable Long serviceId,
            @RequestParam String date
    ) {

        var user = authHelper.getPatientUserFromRequest(request);
        if (user.isEmpty())
            return ResponseEntity.status(401).body("Unauthorized");

        LocalDate targetDate = LocalDate.parse(date);

        var department = departmentRepo.findById(depId);
        if (department.isEmpty())
            return ResponseEntity.badRequest().body("Department not found");

        List<Doctor> doctors = doctorService.findByDepartment(department.get());

        List<DoctorSchedule> available = new ArrayList<>();

        for (Doctor d : doctors) {
            available.addAll(
                    scheduleRepo.findByDoctorAndDateAndBookedFalseOrderByStartTimeAsc(d, targetDate)
            );
        }

        return ResponseEntity.ok(available);
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
