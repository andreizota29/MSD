package com.uaic.mediconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Override
    public void logAction(String userEmail, String action, String details) {
        String timestamp = LocalDateTime.now().toString();
        logger.info("AUDIT [{}] User: {}, Action: {}, Details: {}", timestamp, userEmail, action, details);
    }
}