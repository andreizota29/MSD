package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.*;
import com.uaic.mediconnect.service.AppointmentService;
import com.uaic.mediconnect.service.DepartmentService;
import com.uaic.mediconnect.service.ScheduleGenerator;
import com.uaic.mediconnect.service.UserService;
import jakarta.transaction.Transactional;
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

    @Autowired
    private ScheduleGenerator scheduleGenerator;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DepartmentService departmentService;

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
        List<Doctor> doctors = doctorRepo.findByActiveTrue();
        doctors.forEach(d -> System.out.println(d.getDepartment()));
        return ResponseEntity.ok(doctors);
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctor.getUser() == null || doctor.getDepartment() == null) {
                return ResponseEntity.badRequest().body("Doctor must have a user and department assigned");
            }

            if (!departmentRepo.existsById(doctor.getDepartment().getId())) {
                return ResponseEntity.badRequest().body("Department not found");
            }

            if (doctor.getUser().getPassword() == null || doctor.getUser().getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            doctor.getUser().setPassword(passwordEncoder.encode(doctor.getUser().getPassword()));
            doctor.getUser().setRole(Role.DOCTOR);
            Doctor savedDoctor = doctorRepo.save(doctor);

            List<DoctorSchedule> slots = scheduleGenerator.generate90Days(savedDoctor);
            scheduleRepo.saveAll(slots);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        var doctorOpt = doctorRepo.findById(id);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found");
        }

        Doctor doctor = doctorOpt.get();
        doctor.setActive(false);
        doctorRepo.save(doctor);

        return ResponseEntity.ok(Map.of("message", "Doctor deactivated successfully"));
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
