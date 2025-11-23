package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.PatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private UserService userService;

    public void addPatient(Patient patient){
        patientRepo.save(patient);
    }

    public boolean existsByUser(User user) {
        return patientRepo.findByUser(user).isPresent();
    }

    public Optional<Patient> findByUserId(Long userId) {
        return patientRepo.findByUser_UserId(userId);
    }

    public Optional<Patient> findByUser(User user){
        return  patientRepo.findByUser(user);
    }

    public void deletePatient(Patient patient){
        patientRepo.delete(patient);
    }

    public Optional<Patient> findByCnp(String cnp) { return patientRepo.findByCnp(cnp); }

    public boolean cnpExists(String cnp) {
        return patientRepo.existsByCnp(cnp);
    }

    @Transactional
    public void deletePatientFully(User user){
        Optional<Patient> patientOpt = patientRepo.findByUser(user);
        if(patientOpt.isPresent()){
            Patient patient = patientOpt.get();
            List<Appointment> appointments = appointmentService.findByPatient(patient);

            for(Appointment app: appointments){
                DoctorSchedule slot = app.getDoctorSchedule();
                if(slot != null){
                    slot.setBooked(false);
                    slot.setPatient(null);
                    scheduleRepo.save(slot);
                }
                appointmentService.delete(app);
            }
            patientRepo.delete(patient);
        }
        userService.deleteUser(user);
    }

}
