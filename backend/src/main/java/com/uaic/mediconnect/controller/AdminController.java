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

        var departmentOpt = departmentRepo.findById(id);
        if (departmentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Department not found");
        }
        Department department = departmentOpt.get();
        List<Doctor> doctors = doctorRepo.findByDepartment(department);
        for (Doctor doctor : doctors) {
            doctor.setDepartment(null);
        }
        doctorRepo.saveAll(doctors);
        departmentRepo.delete(department);

        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
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

    @GetMapping("/services")
    public ResponseEntity<List<ClinicService>> getAllServices() {
        List<ClinicService> services = clinicServiceRepo.findAll();
        services.forEach(s -> {
            Department dept = s.getDepartment();
            if (dept != null) {
                // Only keep ID and name to avoid recursion
                Department d = new Department();
                d.setId(dept.getId());
                d.setName(dept.getName());
                s.setDepartment(d);
            }
        });
        return ResponseEntity.ok(services);
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

    @PutMapping("/doctors/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDoctor) {

        var doctorOpt = doctorRepo.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();

        doctor.getUser().setFirstName(updatedDoctor.getUser().getFirstName());
        doctor.getUser().setLastName(updatedDoctor.getUser().getLastName());
        doctor.getUser().setPhone(updatedDoctor.getUser().getPhone());

        if (updatedDoctor.getDepartment() != null) {
            var deptOpt = departmentRepo.findById(updatedDoctor.getDepartment().getId());
            doctor.setDepartment(deptOpt.orElse(null));
        } else {
            doctor.setDepartment(null);
        }

        doctor.setTimetableTemplate(updatedDoctor.getTimetableTemplate());

        userRepo.save(doctor.getUser());

        doctorRepo.save(doctor);

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
