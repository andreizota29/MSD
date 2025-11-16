package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Department;
import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.DoctorSchedule;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.DoctorRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepo doctorRepo;

    @Autowired
    private ScheduleGenerator scheduleGenerator;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    public Doctor createDoctor(Doctor doctor) {

        Doctor saved = doctorRepo.save(doctor);

        List<DoctorSchedule> slots = scheduleGenerator.generate90Days(saved);

        scheduleRepo.saveAll(slots);

        return saved;
    }

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





}
