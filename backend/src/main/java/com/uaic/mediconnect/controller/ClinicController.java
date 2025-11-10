package com.uaic.mediconnect.controller;

import com.uaic.mediconnect.entity.Clinic;
import com.uaic.mediconnect.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clinics")
@CrossOrigin(origins = "http://localhost:4200")
public class ClinicController {

    @Autowired
    private ClinicService clinicService;

    @GetMapping
    public ResponseEntity<List<Clinic>> getAllClinics(){
        List<Clinic> clinics = clinicService.getAllClinics();
        return  ResponseEntity.ok(clinics);
    }

    @PostMapping("/addClinic")
    public ResponseEntity<Clinic> addClinic(@RequestBody Clinic clinic) {
        Clinic savedClinic = clinicService.addClinic(clinic);
        return ResponseEntity.ok(savedClinic);
    }

}
