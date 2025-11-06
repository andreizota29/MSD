package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Pacient;
import com.uaic.mediconnect.entity.User;
import com.uaic.mediconnect.repository.PacientRepo;
import com.uaic.mediconnect.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PacientService {

    @Autowired
    PacientRepo pacientRepo;

    public Pacient addPacient(Pacient pacient){
        return pacientRepo.save(pacient);
    }

    public Optional<Pacient> findByUserId(Long userId) {
        return pacientRepo.findByUserId(userId);
    }

    public Optional<Pacient> findByUser(User user){
        return  pacientRepo.findByUser(user);
    }


}
