package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.*;
import com.uaic.mediconnect.exception.BusinessValidationException;
import com.uaic.mediconnect.repository.AppointmentRepo;
import com.uaic.mediconnect.repository.ClinicServiceRepo;
import com.uaic.mediconnect.repository.DoctorScheduleRepo;
import com.uaic.mediconnect.repository.PatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ValidationServiceImpl implements ValidationService{

    @Autowired
    private DoctorScheduleRepo scheduleRepo;

    @Autowired
    private ClinicServiceRepo serviceRepo;

    @Autowired
    private PatientRepo patientRepo;

    @Autowired
    private AppointmentRepo appointmentRepo;

    @Override
    public void validateAppointmentBooking(Long slotId, Long serviceId, Long patientId){
        if(!patientRepo.existsById(patientId)){
            throw new BusinessValidationException("Patient not found.");
        }

        DoctorSchedule slot = scheduleRepo.findById(slotId)
                .orElseThrow(() -> new BusinessValidationException("Time slot not found."));
        if(slot.isBooked()) {
            throw new BusinessValidationException("This time slot is already booked.");
        }

        ClinicService service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new BusinessValidationException("Service not found."));

        if(slot.getDoctor().getDepartment() != null && service.getDepartment() != null) {
            Long doctorDeptId = slot.getDoctor().getDepartment().getId();
            Long serviceDeptId = service.getDepartment().getId();

            if(!doctorDeptId.equals(serviceDeptId)){
                throw new BusinessValidationException("The selected service is not provided in this doctor's department");
            }
        }
    }

    @Override
    public void validateAppointmentCancellation(Long appointmentId, Long currentUserId){
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new BusinessValidationException("Appointment not found"));
        if(!appointment.getPatient().getUser().getUserId().equals(currentUserId)){
            throw new BusinessValidationException("You are not authorized to cancel the appointment");
        }

        if(appointment.getStatus() == AppointmentStatus.COMPLETED){
            throw new BusinessValidationException("Cannot cancel an appointment that has already been completed");
        }

        if(appointment.getStatus() == AppointmentStatus.CANCELLED){
            throw new BusinessValidationException("Appointment is already cancelled");
        }
    }

    @Override
    public void validateAppointmentAggregate(Appointment appointment) throws BusinessValidationException {
        if(appointment.getDoctor() == null){
            throw new BusinessValidationException("Appointment must have a doctor assigned");
        }
        if(appointment.getPatient() == null){
            throw new BusinessValidationException("Appointment must have a patient assigned");
        }
    }

    @Override
    public void validatePatientProfileData(Patient patient) {
        String cnp = patient.getCnp();
        LocalDate dob = patient.getDateOfBirth();

        if (cnp == null || !cnp.matches("\\d{13}")) {
            throw new BusinessValidationException("CNP must be exactly 13 digits.");
        }
        if (!isValidCnpControlDigit(cnp)) {
            throw new BusinessValidationException("Invalid CNP: Control digit does not match.");
        }

        LocalDate cnpDate = extractDateFromCnp(cnp);
        if (!cnpDate.isEqual(dob)) {
            throw new BusinessValidationException("CNP birthdate (" + cnpDate + ") does not match the provided Date of Birth (" + dob + ")");
        }
    }

    private boolean isValidCnpControlDigit(String cnp) {
        String constant = "279146358279";

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int cnpDigit = Character.getNumericValue(cnp.charAt(i));
            int constDigit = Character.getNumericValue(constant.charAt(i));
            sum += cnpDigit * constDigit;
        }

        int remainder = sum % 11;
        int calculatedControl = (remainder == 10) ? 1 : remainder;
        int actualControl = Character.getNumericValue(cnp.charAt(12));

        return calculatedControl == actualControl;
    }

    private LocalDate extractDateFromCnp(String cnp) {
        int s = Character.getNumericValue(cnp.charAt(0));
        int yearTwoDigit = Integer.parseInt(cnp.substring(1, 3));
        int month = Integer.parseInt(cnp.substring(3, 5));
        int day = Integer.parseInt(cnp.substring(5, 7));

        int century = 0;
        if (s == 1 || s == 2) century = 1900;
        else if (s == 5 || s == 6) century = 2000;
        else if (s == 3 || s == 4) century = 1800;
        else throw new BusinessValidationException("Invalid CNP Gender/Century digit");

        int fullYear = century + yearTwoDigit;

        try {
            return LocalDate.of(fullYear, month, day);
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid date extracted from CNP: " + fullYear + "-" + month + "-" + day);
        }
    }


}
