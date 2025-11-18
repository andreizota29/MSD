package com.uaic.mediconnect.service;

import com.uaic.mediconnect.entity.Appointment;
import com.uaic.mediconnect.entity.Patient;
import com.uaic.mediconnect.exception.BusinessValidationException;

public interface ValidationService {

    void validateAppointmentBooking(Long slotId, Long serviceId, Long patientId) throws BusinessValidationException;
    void validateAppointmentCancellation(Long appointmentId, Long currentUserId) throws BusinessValidationException;
    void validateAppointmentAggregate(Appointment appointment) throws BusinessValidationException;
    void validatePatientProfileData(Patient patient) throws BusinessValidationException;
}
