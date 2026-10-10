package com.example.demo.service;

import com.example.demo.entity.AuditLog;
import com.example.demo.entity.User;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(User user, String action, String targetType, Long targetId) {
        AuditLog log = new AuditLog(user, action, targetType, targetId);
        auditLogRepository.save(log);
    }
}