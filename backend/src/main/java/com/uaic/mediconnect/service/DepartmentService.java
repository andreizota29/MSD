package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ClinicServiceRepo clinicServiceRepo;

    @Transactional
    public void deleteDepartment(Long departmentId){
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        List<Doctor> doctors = doctorRepo.findByDepartment(department);
        for (Doctor doctor : doctors) {
            appointmentService.findByDoctor(doctor)
                    .forEach(appointmentService::cancelAppointment);
        }

        List<ClinicService> services = clinicServiceRepo.findByDepartment(department);
        for (ClinicService service : services) {
            appointmentService.findByService(service)
                    .forEach(appointmentService::cancelAppointment);
        }

        departmentRepo.delete(department);
    }
}
