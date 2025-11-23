package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.DepartmentDTO;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.factory.ClinicFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
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

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private ClinicFactory clinicFactory;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ClinicServiceService clinicServiceService;

    @Transactional
    public void deleteDepartmentFully(Long departmentId, String adminEmail){
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        List<Doctor> doctors = doctorRepo.findByDepartment(department);
        for (Doctor doctor : doctors) {
            doctorService.deleteDoctorFully(doctor.getId(), adminEmail);
        }
        List<ClinicService> services = clinicServiceRepo.findByDepartment(department);
        for (ClinicService service : services) {
            clinicServiceService.deleteServiceFully(service.getId(), adminEmail);
        }
        auditService.logAction(adminEmail, "DELETE_DEPARTMENT", "Deleted ID: " + departmentId);

        departmentRepo.delete(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments(){
        return departmentRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public Department createDepartmentFully(String name, String adminEmail){
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Department name is required");
        }
        if (departmentRepo.findByName(name).isPresent()) {
            throw new RuntimeException("Department already exists");
        }
        Department department = clinicFactory.createDepartment(name);
        auditService.logAction(adminEmail, "CREATE_DEPARTMENT", "Created Department ID: " + department.getId());
        return departmentRepo.save(department);
    }
}
