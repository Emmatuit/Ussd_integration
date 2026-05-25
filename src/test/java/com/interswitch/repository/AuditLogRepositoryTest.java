package com.interswitch.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.interswitch.constant.ErrorCode;
import com.interswitch.constant.TransactionStatus;
import com.interswitch.constant.UssdStep;
import com.interswitch.entity.AuditLog;

@DataJpaTest
class AuditLogRepositoryTest {

	@Autowired
	private AuditLogRepository auditLogRepository;

	@Test
	void shouldPersistAuditLog() {
		AuditLog auditLog = AuditLog.builder().sessionId("session-123").msisdn("2348012345678")
				.step(UssdStep.AIRTIME_INIT).input("*723*1000#").responseText("Select account")
				.status(TransactionStatus.SUCCESS).build();

		AuditLog saved = auditLogRepository.save(auditLog);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getSessionId()).isEqualTo("session-123");
	}

	@Test
	void shouldPersistAuditLogWithErrorCode() {
		AuditLog auditLog = AuditLog.builder().sessionId("session-456").msisdn("2348012345678").step(UssdStep.PURCHASE)
				.status(TransactionStatus.FAILED).errorCode(ErrorCode.TRANSACTION_FAILED).build();

		AuditLog saved = auditLogRepository.save(auditLog);

		assertThat(saved.getErrorCode()).isEqualTo(ErrorCode.TRANSACTION_FAILED);
	}
}