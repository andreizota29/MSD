package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.AppointmentDTO;
import com.uaic.mediconnect.dto.ClinicServiceDTO;
import com.uaic.mediconnect.dto.DepartmentDTO;
import com.uaic.mediconnect.dto.DoctorScheduleDTO;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.exception.BusinessValidationException;
import com.uaic.mediconnect.factory.AppointmentFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.security.JwtUtil;
import com.uaic.mediconnect.service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/patient")
@CrossOrigin(origins = "http://localhost:4200")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuthHelperService authHelper;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ClinicServiceService clinicServiceService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DtoMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request){
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        var patient = patientService.findByUser(userOpt.get());
        if(patient.isEmpty()) return ResponseEntity.badRequest().body("Patient not found");

        return ResponseEntity.ok(mapper.toDTO(patient.get()));
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deleteMyAccount(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        try {
            patientService.deletePatientFully(userOpt.get());
            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/appointments/list")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointmentsList(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        var patient = patientService.findByUser(userOpt.get()).get();
        return ResponseEntity.ok(appointmentService.getScheduledAppointments(patient));
    }


    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> cancelAppointment(HttpServletRequest request, @PathVariable Long id) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        try {
            appointmentService.cancelAppointmentFully(id, userOpt.get().getUserId());
            return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }


    @GetMapping("/departments/{id}/services")
    public ResponseEntity<List<ClinicServiceDTO>> getServicesByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(clinicServiceService.getServicesByDepartment(id));
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(HttpServletRequest request,
                                             @RequestParam Long slotId,
                                             @RequestParam Long serviceId) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");
        var patient = patientService.findByUser(userOpt.get()).get();

        try {
            Long id = appointmentService.bookAppointmentFully(slotId, serviceId, patient.getId());
            return ResponseEntity.ok(Map.of("message", "Appointment booked", "appointmentId", id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/departments/{depId}/services/{serviceId}/slots")
    public ResponseEntity<List<DoctorScheduleDTO>> getAvailableSlots(
            @PathVariable Long depId, @RequestParam String date) {
        try {
            return ResponseEntity.ok(doctorService.getAvailableSlots(depId, date));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
