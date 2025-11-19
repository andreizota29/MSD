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

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AppointmentFactory appointmentFactory;


    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request){
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if(userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        var patientOpt = patientService.findByUser(userOpt.get());
        if(patientOpt.isEmpty()) return ResponseEntity.badRequest().body("Patient not found");

        return ResponseEntity.ok(mapper.toDTO(patientOpt.get()));
    }


    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<?> deleteMyAccount(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid or unauthorized user");
        }

        var user = userOpt.get();
        var patientOpt = patientService.findByUser(user);
        if (patientOpt.isPresent()) {
            Patient patient = patientOpt.get();
            List<Appointment> appointments = appointmentService.findByPatient(patient);

            for (Appointment app : appointments) {
                DoctorSchedule slot = app.getDoctorSchedule();
                if (slot != null) {
                    slot.setBooked(false);
                    slot.setPatient(null);
                    scheduleRepo.save(slot);
                }

                appointmentService.delete(app);
            }
            patientService.deletePatient(patient);
        }
        userService.deleteUser(user);

        return ResponseEntity.ok(Map.of("message", "Account and all data deleted successfully"));
    }

    @GetMapping("/appointments/list")
    public ResponseEntity<List<AppointmentDTO>> getMyAppointmentsList(HttpServletRequest request) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        var patient = patientService.findByUser(userOpt.get()).get();

        List<AppointmentDTO> dtos = appointmentService.findByPatient(patient).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .map(mapper::toDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }


    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> cancelAppointment(HttpServletRequest request, @PathVariable Long id) {
        var userOpt = authHelper.getPatientUserFromRequest(request);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        var patientOpt = patientService.findByUser(userOpt.get());
        if (patientOpt.isEmpty()) return ResponseEntity.badRequest().body("Patient not found");

        try {
            validationService.validateAppointmentCancellation(id, userOpt.get().getUserId());
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        var appointmentOpt = appointmentService.findById(id);
        if (appointmentOpt.isEmpty()) return ResponseEntity.badRequest().body("Appointment not found");
        var appointment = appointmentOpt.get();
        appointmentService.cancelAppointment(appointment);
        try {
            String to = patientOpt.get().getUser().getEmail();
            String subject = "Appointment Cancelled";
            String text = String.format(
                    "Hello %s,\n\nYour appointment with Dr. %s %s on %s at %s has been successfully cancelled.\n\n",
                    patientOpt.get().getUser().getFirstName(),
                    appointment.getDoctor().getUser().getFirstName(),
                    appointment.getDoctor().getUser().getLastName(),
                    appointment.getDoctorSchedule().getDate(),
                    appointment.getDoctorSchedule().getStartTime()
            );
            emailService.sendSimpleEmail(to, subject, text);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "Appointment cancelled successfully"));
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> dtos = departmentRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/departments/{id}/services")
    public ResponseEntity<List<ClinicServiceDTO>> getServicesByDepartment(@PathVariable Long id) {
        List<ClinicServiceDTO> dtos = clinicServiceService.findAllByDepartment(id).stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<?> bookAppointment(
            HttpServletRequest request,
            @RequestParam Long slotId,
            @RequestParam Long serviceId
    ) {

        var user = authHelper.getPatientUserFromRequest(request);
        if (user.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        var patient = patientService.findByUser(user.get()).orElse(null);
        if (patient == null) return ResponseEntity.badRequest().body("Patient not found");
        try {
            validationService.validateAppointmentBooking(slotId, serviceId, patient.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        ClinicService service = clinicServiceService.getServiceById(serviceId);
        DoctorSchedule slot = scheduleRepo.findById(slotId).get();

        if (slot.isBooked()) return ResponseEntity.status(400).body("Slot already booked");

        slot.setBooked(true);
        slot.setPatient(patient);
        scheduleRepo.save(slot);
        Appointment appointment = appointmentFactory.createAppointment(patient, slot, service);
        appointmentService.save(appointment);

        try {
            String patientEmail = patient.getUser().getEmail();
            String patientSubject = "Appointment Confirmation";
            String patientText = String.format(
                    "Hello %s %s,\n\nYour appointment for '%s' [ '%.2f' RON ] with Dr. %s %s on %s at %s has been successfully booked.\n\nThank you!",
                    patient.getUser().getFirstName(), patient.getUser().getLastName(),
                    service.getName(), service.getPrice(),
                    slot.getDoctor().getUser().getFirstName(), slot.getDoctor().getUser().getLastName(),
                    slot.getDate().toString(), slot.getStartTime()
            );
            emailService.sendSimpleEmail(patientEmail, patientSubject, patientText);

            String doctorEmail = slot.getDoctor().getUser().getEmail();
            String doctorSubject = "New Appointment Booked";
            String doctorText = String.format(
                    "Hello Dr. %s,\n\nA new appointment has been booked.\n\n" +
                            "Time: %s at %s\n" +
                            "Service: %s\n\n" +
                            "Patient Details:\n" +
                            "Name: %s %s\n" +
                            "Phone: %s\n" +
                            "Email: %s\n" +
                            "CNP: %s\n" +
                            "DOB: %s",
                    slot.getDoctor().getUser().getLastName(),
                    slot.getDate(), slot.getStartTime(),
                    service.getName(),
                    patient.getUser().getFirstName(), patient.getUser().getLastName(),
                    patient.getUser().getPhone(),
                    patient.getUser().getEmail(),
                    patient.getCnp(),
                    patient.getDateOfBirth()
            );
            emailService.sendSimpleEmail(doctorEmail, doctorSubject, doctorText);

        } catch (Exception e) {
            System.err.println("Failed to send confirmation emails: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "message", "Appointment booked successfully",
                "appointmentId", appointment.getId()
        ));
    }



    @GetMapping("/departments/{depId}/services/{serviceId}/slots")
    public ResponseEntity<List<DoctorScheduleDTO>> getAvailableSlots(
            HttpServletRequest request,
            @PathVariable Long depId,
            @PathVariable Long serviceId,
            @RequestParam String date
    ) {
        LocalDate targetDate = LocalDate.parse(date);
        LocalTime currentTime = LocalTime.now();

        var department = departmentRepo.findById(depId).orElse(null);
        if (department == null) return ResponseEntity.badRequest().build();

        List<Doctor> doctors = doctorService.findByDepartment(department);
        List<DoctorScheduleDTO> availableDtos = new ArrayList<>();

        for (Doctor d : doctors) {
            List<DoctorSchedule> slots = scheduleRepo.findByDoctorAndDateAndBookedFalseOrderByStartTimeAsc(d, targetDate);

            if (targetDate.equals(LocalDate.now())) {
                slots = slots.stream()
                        .filter(s -> s.getStartTime().isAfter(currentTime))
                        .collect(Collectors.toList());
            }

            availableDtos.addAll(slots.stream().map(mapper::toDTO).toList());
        }

        return ResponseEntity.ok(availableDtos);
    }

}
