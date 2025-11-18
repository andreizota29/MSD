package com.uaic.mediconnect.service;

public interface AuditService {
    void logAction(String userEmail, String action, String details);
}
