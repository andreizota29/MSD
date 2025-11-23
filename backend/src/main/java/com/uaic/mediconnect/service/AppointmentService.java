package com.uaic.mediconnect.service;

import com.uaic.mediconnect.dto.AppointmentDTO;
import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.factory.AppointmentFactory;
import com.uaic.mediconnect.mapper.DtoMapper;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.PatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AppointmentFactory appointmentFactory;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DtoMapper mapper;

    public List<Appointment> findByPatient(Patient patient) {
        return appointmentRepo.findByPatient(patient);
    }

    public List<Appointment> findByDoctor(Doctor doctor){
        return appointmentRepo.findByDoctor(doctor);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepo.findById(id);
    }

    public boolean existsByDoctor(Doctor doctor) {
        return appointmentRepo.existsByDoctor(doctor);
    }

    public List<Appointment> findByService(ClinicService service){
        return appointmentRepo.findByService(service);
    }

    public void delete(Appointment appointment) {
        appointmentRepo.delete(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorAppointments(Doctor doctor){
        return appointmentRepo.findByDoctor(doctor).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<AppointmentDTO> getScheduledAppointments(Patient patient){
        return appointmentRepo.findByPatient(patient).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public Long bookAppointmentFully(Long slotId, Long serviceId, Long patientId) {
        validationService.validateAppointmentBooking(slotId, serviceId, patientId);
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow();
        ClinicService service = serviceRepo.findById(serviceId)
                .orElseThrow();
        DoctorSchedule slot = scheduleRepo.findById(slotId)
                .orElseThrow();

        if(slot.isBooked()){
            throw new RuntimeException("Slot already booked");
        }

        slot.setBooked(true);
        slot.setPatient(patient);
        scheduleRepo.save(slot);
        Appointment appointment = appointmentFactory.createAppointment(patient, slot, service);
        Appointment saved = appointmentRepo.save(appointment);
        sendBookingEmails(patient,slot,service);
        return saved.getId();
    }

    @Transactional
    public void cancelAppointmentFully(Long appointmentId, Long userId){
        validationService.validateAppointmentCancellation(appointmentId,userId);
        Appointment appointment = appointmentRepo.findById(appointmentId).orElseThrow();
        Patient patient = appointment.getPatient();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(appointment);
        DoctorSchedule slot = appointment.getDoctorSchedule();
        if(slot != null) {
            slot.setBooked(false);
            slot.setPatient(null);
            scheduleRepo.save(slot);
        }
        sendCancellationEmail(patient, appointment);
    }

    private void sendBookingEmails(Patient patient, DoctorSchedule slot, ClinicService service) {
        try {
            String patientText = String.format(
                    "Hello %s,\n\nYour appointment for %s [ %.2f RON ] with Dr. %s %s on %s at %s has been booked.",
                    patient.getUser().getFirstName(), service.getName(), service.getPrice(),
                    slot.getDoctor().getUser().getFirstName(), slot.getDoctor().getUser().getLastName(),
                    slot.getDate(), slot.getStartTime()
            );
            emailService.sendSimpleEmail(patient.getUser().getEmail(), "Appointment Confirmation", patientText);

            String doctorText = String.format(
                    "Hello Dr. %s,\nNew appointment:\nPatient: %s %s\nPhone: %s\nDate: %s %s",
                    slot.getDoctor().getUser().getLastName(),
                    patient.getUser().getFirstName(), patient.getUser().getLastName(),
                    patient.getUser().getPhone(),
                    slot.getDate(), slot.getStartTime()
            );
            emailService.sendSimpleEmail(slot.getDoctor().getUser().getEmail(), "New Booking", doctorText);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }

    private void sendCancellationEmail(Patient patient, Appointment app) {
        try {
            String text = String.format("Hello %s,\nYour appointment with Dr. %s on %s has been cancelled.",
                    patient.getUser().getFirstName(),
                    app.getDoctor().getUser().getLastName(),
                    app.getDoctorSchedule().getDate());
            emailService.sendSimpleEmail(patient.getUser().getEmail(), "Appointment Cancelled", text);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}
