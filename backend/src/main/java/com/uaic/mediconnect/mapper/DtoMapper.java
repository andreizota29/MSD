package com.uaic.mediconnect.mapper;

import com.uaic.mediconnect.dto.*;
import com.uaic.mediconnect.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public DepartmentDTO toDTO(Department dep){
        if (dep == null) return null;
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(dep.getId());
        dto.setName(dep.getName());
        return dto;
    }

    public UserDTO toDTO(User user){
        if(user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        user.setRole(user.getRole());
        dto.setProfileCompleted(user.isProfileCompleted());
        return dto;
    }

    public ClinicServiceDTO toDTO(ClinicService service) {
        ClinicServiceDTO dto = new ClinicServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setPrice(service.getPrice());
        dto.setDepartment(toDTO(service.getDepartment()));
        return dto;
    }

    public DoctorDTO toDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setTitle(doctor.getTitle());
        dto.setActive(doctor.isActive());
        dto.setTimetableTemplate(doctor.getTimetableTemplate());
        dto.setUser(toDTO(doctor.getUser()));
        dto.setDepartment(toDTO(doctor.getDepartment()));
        return dto;
    }

    public PatientDTO toDTO(Patient patient) {
        if (patient == null) return null;
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setCnp(patient.getCnp());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setUser(toDTO(patient.getUser()));
        return dto;
    }

    public DoctorScheduleDTO toDTO(DoctorSchedule slot) {
        DoctorScheduleDTO dto = new DoctorScheduleDTO();
        dto.setId(slot.getId());
        dto.setDate(slot.getDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setBooked(slot.isBooked());
        dto.setDoctor(toDTO(slot.getDoctor()));
        dto.setPatient(toDTO(slot.getPatient()));
        if (slot.getAppointments() != null) {
            Appointment activeAppointment = slot.getAppointments().stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.COMPLETED)
                    .findFirst()
                    .orElse(null);

            if (activeAppointment != null && activeAppointment.getService() != null) {
                dto.setServiceName(activeAppointment.getService().getName());
            }
        }
        return dto;
    }

    public AppointmentDTO toDTO(Appointment appt) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appt.getId());
        dto.setStatus(appt.getStatus());
        dto.setService(toDTO(appt.getService()));
        dto.setDoctor(toDTO(appt.getDoctor()));
        dto.setDoctorSchedule(toDTO(appt.getDoctorSchedule()));
        dto.setPatient(toDTO(appt.getPatient()));
        return dto;
    }


}
