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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClinicServiceRepo clinicServiceRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ScheduleGenerator scheduleGenerator;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ClinicServiceService clinicServiceService;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private ClinicFactory clinicFactory;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments(){
        List<DepartmentDTO> dtos = departmentRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping(value = "/departments", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createDepartment(@RequestBody Department input) {
        if (input.getName() == null || input.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Department name is required");
        }
        Department department = clinicFactory.createDepartment(input.getName());
        Department saved = departmentRepo.save(department);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }


    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        System.out.println("Received delete request for department ID: " + id);
        try {
            departmentService.deleteDepartment(id);
            System.out.println("Department deleted successfully: " + id);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        } catch (RuntimeException e) {
            System.out.println("Department deletion failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/services")
    public ResponseEntity<?> createService(@RequestBody CreateServiceRequest req) {
        if (req.getName() == null || req.getDepartment() == null) {
            return ResponseEntity.badRequest().body("Missing data");
        }
        Department dept = departmentRepo.findById(req.getDepartment().getId())
                .orElseThrow(() -> new RuntimeException("Dept not found"));

        ClinicService service = clinicFactory.createService(req, dept);
        ClinicService saved = clinicServiceRepo.save(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        var serviceOpt = clinicServiceRepo.findById(id);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service not found");
        }
        clinicServiceRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Service deleted successfully"));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ClinicServiceDTO>> getAllServices() {
        List<ClinicServiceDTO> dtos = clinicServiceRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
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
