package com.interswitch.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.interswitch.constant.ErrorCode;
import com.interswitch.constant.TransactionStatus;
import com.interswitch.constant.UssdStep;
import com.interswitch.entity.AuditLog;
import com.interswitch.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	@Async
	public void logRequest(String sessionId, String msisdn, UssdStep step, String input, String responseText,
			TransactionStatus status, ErrorCode errorCode) {
		try {
			AuditLog auditLog = AuditLog.builder().sessionId(sessionId).msisdn(msisdn).step(step).input(input)
					.responseText(responseText).status(status).errorCode(errorCode).build();

			auditLogRepository.save(auditLog);
			log.debug("Audit log saved for session: {}, step: {}", sessionId, step);
		} catch (Exception e) {
			log.error("Failed to save audit log for session: {}", sessionId, e);
		}
	}
}