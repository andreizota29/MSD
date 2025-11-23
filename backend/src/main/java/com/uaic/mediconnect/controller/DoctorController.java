package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.dto.AppointmentDTO;
import com.uaic.mediconnect.dto.DoctorScheduleDTO;
import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.service.AppointmentService;
import com.uaic.mediconnect.service.AuthHelperService;
import com.uaic.mediconnect.service.DoctorService;
import com.uaic.mediconnect.service.ScheduleGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/doctor")
@CrossOrigin(origins = "http://localhost:4200")
public class DoctorController {

    @Autowired
    private AuthHelperService authHelperService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

//    @GetMapping("/appointments")
//    public ResponseEntity<?> getMyAppointments(HttpServletRequest request){
//        var userOpt = authHelperService.getDoctorUserFromRequest(request);
//        if(userOpt.isEmpty()){
//            return ResponseEntity.status(401).body("Unauthorized");
//        }
//        var doctorOpt = doctorService.findByUser(userOpt.get());
//        if(doctorOpt.isEmpty()){
//            return ResponseEntity.badRequest().body("Doctor not found");
//        }
//        List<Appointment> appointments = appointmentService.findByDoctor(doctorOpt.get());
//        return ResponseEntity.ok(appointments);
//    }

    private Optional<Doctor> getAuthenticatedDoctor(HttpServletRequest request) {
        return authHelperService.getDoctorUserFromRequest(request)
                .flatMap(doctorService::findByUser);
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request){
        Optional<Doctor> doctorOpt = getAuthenticatedDoctor(request);
        if(doctorOpt.isEmpty()){
            return ResponseEntity.status(401).body("Unauthorized or Doctor not found");
        }
        List<AppointmentDTO> appointments = appointmentService.getDoctorAppointments(doctorOpt.get());
        return ResponseEntity.ok(appointments);

    }

    @GetMapping("/timetable")
    public ResponseEntity<?> getTimetable(HttpServletRequest request){
        Optional<Doctor> doctorOpt = getAuthenticatedDoctor(request);
        if(doctorOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<DoctorScheduleDTO> slots = doctorService.getAllTimetable(doctorOpt.get());
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/timetable/week")
    public ResponseEntity<List<DoctorScheduleDTO>> getWeeklyTimetable(HttpServletRequest request, @RequestParam String start){
        Optional<Doctor> doctorOpt = getAuthenticatedDoctor(request);
        if(doctorOpt.isEmpty()) return ResponseEntity.status(401).build();

        List<DoctorScheduleDTO> dtos = doctorService.getWeeklyTimetable(doctorOpt.get(), start);
        return ResponseEntity.ok(dtos);

    }

}
