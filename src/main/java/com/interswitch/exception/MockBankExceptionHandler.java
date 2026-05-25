package com.interswitch.exception;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.interswitch.dto.response.TransactionResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Profile("h2")
@Slf4j
public class MockBankExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<TransactionResponse> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Mock bank error: {}", ex.getMessage());
		return ResponseEntity.badRequest()
				.body(TransactionResponse.builder().transactionId(UUID.randomUUID().toString()).status("FAILED")
						.message(ex.getMessage()).referenceNumber("ERR-" + System.currentTimeMillis()).amount("0")
						.build());
	}
}