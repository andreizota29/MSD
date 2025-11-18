package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.ClinicServiceDTO;
import com.uaic.mediconnect.dto.DepartmentDTO;
import com.uaic.mediconnect.entity.ClinicService;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicServiceService {

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private AppointmentService appointmentService;

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
    public void deleteService(Long serviceId) {
        ClinicService service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        appointmentService.findByService(service)
                .forEach(appointmentService::cancelAppointment);

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