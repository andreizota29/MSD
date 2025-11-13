package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Doctor;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.DoctorRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepo doctorRepo;

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
}
