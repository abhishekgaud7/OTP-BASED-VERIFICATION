package com.otp.verification.service;

import com.otp.verification.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    void logAction(String action, String entityType, Long entityId, String details, String status);

    List<AuditLog> getAuditLogs(String entityType, Long entityId);

    List<AuditLog> getAuditLogsByAction(String action);

    List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
