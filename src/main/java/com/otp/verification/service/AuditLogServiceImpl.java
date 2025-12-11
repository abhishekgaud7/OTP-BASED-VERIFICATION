package com.otp.verification.service;

import com.otp.verification.entity.AuditLog;
import com.otp.verification.repository.AuditLogRepository;
import com.otp.verification.util.IpAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final IpAddressUtil ipAddressUtil;

    @Override
    public void logAction(String action, String entityType, Long entityId, String details, String status) {
        try {
            String ipAddress = getClientIpAddress();

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .status(status)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved - Action: {}, Status: {}", action, status);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    @Override
    public List<AuditLog> getAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    @Override
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return ipAddressUtil.getClientIpAddress(request);
            }
        } catch (Exception e) {
            log.debug("Could not retrieve client IP address", e);
        }
        return "UNKNOWN";
    }
}
