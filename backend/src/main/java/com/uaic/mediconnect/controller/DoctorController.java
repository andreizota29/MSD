package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.service.AppointmentService;
import com.uaic.mediconnect.service.AuthHelperService;
import com.uaic.mediconnect.service.DoctorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> getDoctorTimetable(HttpServletRequest request){
        var userOpt = authHelperService.getDoctorUserFromRequest(request);
        if(userOpt.isEmpty()) return  ResponseEntity.status(401).body("Unauthorized");

        var doctorOpt = doctorService.findByUser(userOpt.get());
        if(doctorOpt.isEmpty()) return ResponseEntity.badRequest().body("Doctor not found");
        List<DoctorSchedule> timetable = scheduleRepo.findByDoctorOrderByDateAscStartTimeAsc(doctorOpt.get());
        return ResponseEntity.ok(timetable);
    }
}
