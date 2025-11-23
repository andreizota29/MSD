package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.ClinicServiceDTO;
import com.uaic.mediconnect.dto.CreateServiceRequest;
import com.uaic.mediconnect.dto.DepartmentDTO;
import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.factory.ClinicFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DepartmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicServiceService {

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DtoMapper mapper;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private ClinicFactory clinicFactory;

    @Autowired
    private AuditService auditService;

    public ClinicService getServiceById(Long id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service ID"));
    }

    public List<ClinicService> findAllByDepartment(Long departmentId) {
        return serviceRepo.findAll().stream()
                .filter(s -> s.getDepartment().getId().equals(departmentId))
                .toList();
    }



    @Transactional
    public List<ClinicServiceDTO> getServices() {
        return serviceRepo.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<ClinicServiceDTO> getServicesByDepartment(Long departmentId) {
        return serviceRepo.findAll().stream()
                .map(mapper::toDTO)
                .filter(s -> s.getDepartment().getId().equals(departmentId))
                .toList();
    }

    @Transactional
    public ClinicService createServiceFully(CreateServiceRequest req, String adminEmail) {
        Department dept = departmentRepo.findById(req.getDepartment().getId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        ClinicService service = clinicFactory.createService(req, dept);
        ClinicService saved = serviceRepo.save(service);

        auditService.logAction(adminEmail, "CREATE_SERVICE", "Created Service ID: " + saved.getId());
        return saved;
    }

    @Transactional
    public void deleteServiceFully(Long serviceId, String adminEmail) {
        ClinicService service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        List<Appointment> appointments = appointmentService.findByService(service);
        for(Appointment app : appointments) {
            if(app.getDoctorSchedule() != null) {
                app.getDoctorSchedule().setBooked(false);
                app.getDoctorSchedule().setPatient(null);
            }
            appointmentService.delete(app);
        }

        auditService.logAction(adminEmail, "DELETE_SERVICE", "Deleted ID: " + serviceId);
        serviceRepo.delete(service);
    }

    public List<ClinicServiceDTO> getAllServicesAsDTO() {
        List<ClinicService> entities = serviceRepo.findAll();
        return entities.stream().map(this::convertToDTO).toList();
    }

    private ClinicServiceDTO convertToDTO(ClinicService entity){
        ClinicServiceDTO dto = new ClinicServiceDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());

        if (entity.getDepartment() != null) {
            DepartmentDTO depDto = new DepartmentDTO();
            depDto.setId(entity.getDepartment().getId());
            depDto.setName(entity.getDepartment().getName());

            dto.setDepartment(depDto);
        }
        return dto;
    }
}