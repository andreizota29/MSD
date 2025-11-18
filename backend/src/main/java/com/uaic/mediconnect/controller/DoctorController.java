package com.uaic.mediconnect.controller;

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

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private DtoMapper mapper;

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(HttpServletRequest request){
        var userOpt = authHelperService.getDoctorUserFromRequest(request);
        if(userOpt.isEmpty()){
            return ResponseEntity.status(401).body("Unauthorized");
        }
        var doctorOpt = doctorService.findByUser(userOpt.get());
        if(doctorOpt.isEmpty()){
            return ResponseEntity.badRequest().body("Doctor not found");
        }
        List<Appointment> appointments = appointmentService.findByDoctor(doctorOpt.get());
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/timetable")
    public ResponseEntity<?> getTimetable(HttpServletRequest request){
        var doctor = authHelperService.getDoctorUserFromRequest(request)
                .flatMap(doctorService::findByUser);

        if (doctor.isEmpty())
            return ResponseEntity.status(401).body("Unauthorized");

        List<DoctorSchedule> slots =
                scheduleRepo.findByDoctorOrderByDateAscStartTimeAsc(doctor.get());

        return ResponseEntity.ok(slots);
    }

    @GetMapping("/timetable/week")
    public ResponseEntity<List<DoctorScheduleDTO>> getWeeklyTimetable(
            HttpServletRequest request, @RequestParam String start) {

        var doctorOpt = authHelperService.getDoctorUserFromRequest(request)
                .flatMap(doctorService::findByUser);

        if (doctorOpt.isEmpty()) return ResponseEntity.status(401).build();

        LocalDate monday = LocalDate.parse(start);
        LocalDate sunday = monday.plusDays(6);

        List<DoctorScheduleDTO> dtos = scheduleRepo.findByDoctorAndDateBetweenOrderByDateAscStartTimeAsc(
                        doctorOpt.get(), monday, sunday)
                .stream()
                .map(mapper::toDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
