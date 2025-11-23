package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.CreateDoctorRequest;
import com.uaic.mediconnect.dto.DoctorDTO;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.factory.UserFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.DepartmentRepo;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ScheduleGenerator scheduleGenerator;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private DtoMapper mapper;
    

    public Doctor saveDoctor(Doctor doctor){
        return doctorRepo.save(doctor);
    }

    public Optional<Doctor> findByUser(User user){
        return doctorRepo.findByUser_UserId(user.getUserId());
    }

    public List<Doctor> getAllDoctors(){
        return doctorRepo.findAll();
    }

    public void deleteDoctor(Long id){
        doctorRepo.deleteById(id);
    }

    public List<Doctor> findByDepartment(Department department) {
        return doctorRepo.findByDepartment(department).stream()
                .filter(Doctor::isActive)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctorsFully() {
        return doctorRepo.findByActiveTrue().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public Optional<Doctor> findByUserEmail(String email) { return doctorRepo.findByUserEmail(email);};

    @Transactional
    public Doctor createDoctorFully(CreateDoctorRequest req, String adminEmail) {
        if (userRepo.existsByEmail(req.getUserData().getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        if (userRepo.existsByPhone(req.getUserData().getPhone())) {
            throw new RuntimeException("Phone number is already in use");
        }

        Department dept = departmentRepo.findById(req.getDepartment().getId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Doctor doctor = userFactory.createDoctorAggregate(req, dept);

        Doctor savedDoctor = doctorRepo.save(doctor);

        List<DoctorSchedule> slots = scheduleGenerator.generate90Days(savedDoctor);
        scheduleRepo.saveAll(slots);

        auditService.logAction(adminEmail, "CREATE_DOCTOR", "Created Doctor ID: " + savedDoctor.getId());

        return savedDoctor;
    }

    @Transactional
    public void deleteDoctorFully(Long doctorId, String adminEmail){
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.getServices().clear();
        doctorRepo.save(doctor);

        List<Appointment> appointments = appointmentService.findByDoctor(doctor);
        for( Appointment app: appointments) {
            appointmentService.delete(app);
        }
        scheduleRepo.deleteAllByDoctor(doctor);
        auditService.logAction(adminEmail, "DELETE_DOCTOR", "Deleted ID: " + doctorId);
        doctorRepo.delete(doctor);
    }

    @Transactional
    public void updateDoctorFully(Long doctorId, Doctor updatedDoctor, String adminEmail){
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        boolean templateChanged = updatedDoctor.getTimetableTemplate() != null &&
                updatedDoctor.getTimetableTemplate() != doctor.getTimetableTemplate();

        doctor.getUser().setFirstName(updatedDoctor.getUser().getFirstName());
        doctor.getUser().setLastName(updatedDoctor.getUser().getLastName());
        doctor.setTitle(updatedDoctor.getTitle());

        String newPhone = updatedDoctor.getUser().getPhone();
        String oldPhone = doctor.getUser().getPhone();

        String newEmail = updatedDoctor.getUser().getEmail();
        String oldEmail = doctor.getUser().getEmail();

        if((newEmail != null && !newEmail.equals(oldEmail)) ||
                (newPhone != null && !newPhone.equals(oldPhone))){
            if(userRepo.existsByEmail(newEmail)){
                throw new RuntimeException("Email is already in use by another user");
            }
            if(userRepo.existsByPhone(newPhone)){
                throw new RuntimeException("Phone is already in use by another phone");
            }
            doctor.getUser().setEmail(newEmail);
            doctor.getUser().setPhone(newPhone);
        }

        if(updatedDoctor.getUser().getPassword() != null &&
                !updatedDoctor.getUser().getPassword().isBlank()) {
            doctor.getUser().setPassword(
                    passwordEncoder.encode(updatedDoctor.getUser().getPassword())
            );
        }
        if(updatedDoctor.getDepartment() != null) {
            var dept = departmentRepo.findById(updatedDoctor.getDepartment().getId());
            doctor.setDepartment(dept.orElse(null));
        } else {
            doctor.setDepartment(null);
        }

        if (updatedDoctor.getTimetableTemplate() != null) {
            doctor.setTimetableTemplate(updatedDoctor.getTimetableTemplate());
        }
        userRepo.save(doctor.getUser());
        doctorRepo.save(doctor);
        if(templateChanged) {
            List<Appointment> appointments = appointmentService.findByDoctor(doctor);
            for( Appointment app: appointments) {
                appointmentService.delete(app);
            }
            scheduleRepo.deleteAllByDoctor(doctor);
            List<DoctorSchedule> newSlots = scheduleGenerator.generate90Days(doctor);
            scheduleRepo.saveAll(newSlots);
        }
        auditService.logAction(adminEmail, "UPDATE_DOCTOR", "Updated ID: " + doctorId);
    }
}
