package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.*;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.factory.ClinicFactory;
import com.uaic.mediconnect.factory.UserFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.*;
import com.uaic.mediconnect.service.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {


    @Autowired
    private ScheduleGenerator scheduleGenerator;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ClinicServiceService clinicServiceService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DtoMapper mapper;


    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments(){
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return  ResponseEntity.ok(departments);
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Department input){
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            Department saved = departmentService.createDepartmentFully(input.getName(), currentAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            departmentService.deleteDepartmentFully(id, currentAdmin);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error",e.getMessage()));
        }
    }

    @GetMapping("/services")
    public ResponseEntity<List<ClinicServiceDTO>> getAllServices() {
        List<ClinicServiceDTO> services = clinicServiceService.getServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/departments/{id}/services")
    public ResponseEntity<List<ClinicServiceDTO>> getServicesByDepartment(@PathVariable Long id) {
        List<ClinicServiceDTO> services = clinicServiceService.getServicesByDepartment(id);
        return ResponseEntity.ok(services);
    }

    @PostMapping("/services")
    public ResponseEntity<?> createService(@RequestBody CreateServiceRequest req) {
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            if (req.getName() == null || req.getDepartment() == null) {
                return ResponseEntity.badRequest().body("Missing data");
            }
            ClinicService saved = clinicServiceService.createServiceFully(req, currentAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            clinicServiceService.deleteServiceFully(id, currentAdmin);
            return ResponseEntity.ok(Map.of("message", "Service deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors(){
        List<DoctorDTO> doctors = doctorService.getAllDoctorsFully();
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody CreateDoctorRequest req) {
        if (req.getUserData() == null || req.getDepartment() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
        }
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            Doctor savedDoctor = doctorService.createDoctorFully(req, currentAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(savedDoctor));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id){
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            doctorService.deleteDoctorFully(id, currentAdmin);
            return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
        } catch (RuntimeException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor){
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            doctorService.updateDoctorFully(id, doctor, currentAdmin);
            return ResponseEntity.ok(Map.of("message", "Doctor updated successfully"));
        } catch (RuntimeException e){
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/timetable-templates")
    public ResponseEntity<List<String>> getTimetableTemplates() {
        List<String> templates = Arrays.stream(TimetableTemplate.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(templates);
    }
}
