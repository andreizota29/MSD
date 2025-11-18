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
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        List<DoctorDTO> dtos = doctorRepo.findByActiveTrue().stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@Valid @RequestBody CreateDoctorRequest req) {
        if (req.getUserData() == null || req.getDepartment() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
        }

        if (userRepo.existsByEmail(req.getUserData().getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }
        if (userRepo.existsByPhone(req.getUserData().getPhone())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is already in use"));
        }

        Department dept = departmentRepo.findById(req.getDepartment().getId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Doctor doctor = userFactory.createDoctorAggregate(req, dept);
        Doctor savedDoctor = doctorRepo.save(doctor);

        List<DoctorSchedule> slots = scheduleGenerator.generate90Days(savedDoctor);
        scheduleRepo.saveAll(slots);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(savedDoctor));
    }

    @DeleteMapping("/doctors/{id}")
    @Transactional
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        var doctorOpt = doctorRepo.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();
        scheduleRepo.deleteAllByDoctor(doctor);

        List<Appointment> appointments = appointmentService.findByDoctor(doctor);
        for (Appointment app : appointments) {
            app.setDoctor(null);
            appointmentService.save(app);

            if (app.getStatus() == AppointmentStatus.SCHEDULED) {
                app.setStatus(AppointmentStatus.CANCELLED);
            }
        }

        doctorRepo.delete(doctor);
        String currentAdmin = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.logAction(currentAdmin, "DELETE_DOCTOR", "Deleted Doctor ID: " + id + " (" + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName() + ")");

        return ResponseEntity.ok(Map.of("message", "Doctor and User account deleted permanently"));
    }

    @PutMapping("/doctors/{id}")
    @Transactional
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDoctor) {

        var doctorOpt = doctorRepo.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();

        boolean templateChanged = updatedDoctor.getTimetableTemplate() != null &&
                updatedDoctor.getTimetableTemplate() != doctor.getTimetableTemplate();

        doctor.getUser().setFirstName(updatedDoctor.getUser().getFirstName());
        doctor.getUser().setLastName(updatedDoctor.getUser().getLastName());
        doctor.getUser().setPhone(updatedDoctor.getUser().getPhone());

        if (updatedDoctor.getUser().getPassword() != null &&
                !updatedDoctor.getUser().getPassword().isBlank()) {

            doctor.getUser().setPassword(
                    passwordEncoder.encode(updatedDoctor.getUser().getPassword())
            );
        }

        if (updatedDoctor.getDepartment() != null) {
            var deptOpt = departmentRepo.findById(updatedDoctor.getDepartment().getId());
            doctor.setDepartment(deptOpt.orElse(null));
        } else {
            doctor.setDepartment(null);
        }

        if (updatedDoctor.getTimetableTemplate() != null) {
            doctor.setTimetableTemplate(updatedDoctor.getTimetableTemplate());
        }


        userRepo.save(doctor.getUser());
        doctorRepo.save(doctor);

        if (templateChanged) {
            scheduleRepo.deleteAllByDoctor(doctor);
            List<DoctorSchedule> newSlots = scheduleGenerator.generate90Days(doctor);
            scheduleRepo.saveAll(newSlots);
        }

        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/timetable-templates")
    public ResponseEntity<List<String>> getTimetableTemplates() {
        List<String> templates = Arrays.stream(TimetableTemplate.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(templates);
    }
}
