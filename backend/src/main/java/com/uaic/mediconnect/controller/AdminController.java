package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.UserRepo;
import com.uaic.mediconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments(){
        return ResponseEntity.ok(departmentRepo.findAll());
    }

    @PostMapping(value = "/departments", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        if (department.getName() == null || department.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Department name is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentRepo.save(department));
    }


    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        if (!departmentRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Department not found");
        }
        departmentRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ClinicService>> getAllServices() {
        return ResponseEntity.ok(clinicServiceRepo.findAll());
    }

    @PostMapping(value = "/services", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClinicService> createOrUpdateService(@RequestBody ClinicService serviceData) {
        if (serviceData.getName() == null || serviceData.getName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        var departmentOpt = departmentRepo.findById(serviceData.getDepartment().getId());
        if (departmentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        serviceData.setDepartment(departmentOpt.get());
        var savedService = clinicServiceRepo.save(serviceData);

        // Return only the saved object
        return ResponseEntity.status(HttpStatus.CREATED).body(savedService);
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

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepo.findAll());
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        if (doctor.getUser() == null || doctor.getDepartment() == null) {
            return ResponseEntity.badRequest().body("Doctor must have a user and department assigned");
        }

        doctor.getUser().setRole(Role.DOCTOR);
        doctor.getUser().setPassword(passwordEncoder.encode(doctor.getUser().getPassword()));

        User savedUser = userRepo.save(doctor.getUser());
        doctor.setUser(savedUser);

        Doctor savedDoctor = doctorRepo.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctor);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        if (!doctorRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }
        doctorRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Doctor deleted successfully"));
    }



    @GetMapping("/timetable-templates")
    public ResponseEntity<List<String>> getTimetableTemplates() {
        List<String> templates = Arrays.stream(TimetableTemplate.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(templates);
    }
}
