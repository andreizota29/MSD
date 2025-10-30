package com.uaic.mediconnect.entity;

import jakarta.persistence.Entity;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Doctor extends Staff{
    private String specialty;
    private String licenseNumber;
    private BigDecimal consultationFee;
    private String availability;
}
