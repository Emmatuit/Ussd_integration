package com.interswitch.entity;

import java.time.Instant;
import java.util.UUID;

import com.interswitch.constant.ErrorCode;
import com.interswitch.constant.TransactionStatus;
import com.interswitch.constant.UssdStep;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private String sessionId;

	@Column(nullable = false, length = 20)
	private String msisdn;

	@Column(nullable = false, length = 50)
	@Enumerated(EnumType.STRING)
	private UssdStep step;

	@Column(columnDefinition = "TEXT")
	private String input;

	@Column(name = "response_text", columnDefinition = "TEXT")
	private String responseText;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private TransactionStatus status;

	@Column(name = "error_code", length = 50)
	@Enumerated(EnumType.STRING)
	private ErrorCode errorCode;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
	}
}