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

//    @Transactional
//    public void deleteDepartment(Long departmentId){
//        System.out.println("Attempting to delete department with ID: " + departmentId);
//
//        Department department = departmentRepo.findById(departmentId)
//                .orElseThrow(() -> {
//                    System.out.println("Department not found for ID: " + departmentId);
//                    return new RuntimeException("Department not found");
//                });
//
//        System.out.println("Found department: " + department.getName());
//
//
//        List<Doctor> doctors = doctorRepo.findByDepartment(department);
//        System.out.println("Found " + doctors.size() + " doctors in this department.");
//        for (Doctor doctor : doctors) {
//            List<Appointment> doctorAppointments = appointmentRepo.findByDoctor(doctor);
//            System.out.println("Deleting " + doctorAppointments.size() + " appointments for doctor " + doctor.getUser().getFirstName());
//            appointmentRepo.deleteAll(doctorAppointments);
//        }
//
//        List<ClinicService> services = clinicServiceRepo.findByDepartment(department);
//        System.out.println("Found " + services.size() + " services in this department.");
//        for (ClinicService service : services) {
//            List<Appointment> serviceAppointments = appointmentRepo.findByService(service);
//            System.out.println("Deleting " + serviceAppointments.size() + " appointments for service " + service.getName());
//            appointmentRepo.deleteAll(serviceAppointments);
//        }
//
//        System.out.println("Deleting department: " + department.getName());
//        departmentRepo.delete(department);
//        System.out.println("Department deleted successfully!");
//    }

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
