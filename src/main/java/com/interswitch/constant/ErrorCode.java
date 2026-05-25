package com.interswitch.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_SIGNATURE("USSD_001", "Invalid request signature", HttpStatus.UNAUTHORIZED),
	SIGNATURE_EXPIRED("USSD_002", "Request timestamp expired", HttpStatus.UNAUTHORIZED),
	DECRYPTION_FAILED("USSD_003", "Failed to decrypt request body", HttpStatus.BAD_REQUEST),
	INVALID_REQUEST("USSD_004", "Invalid request payload", HttpStatus.BAD_REQUEST),
	INVALID_STEP("USSD_005", "Invalid USSD step", HttpStatus.BAD_REQUEST),
	NO_ACCOUNTS_FOUND("USSD_006", "No accounts found for this number", HttpStatus.NOT_FOUND),
	INVALID_ACCOUNT_SELECTION("USSD_007", "Invalid account selection", HttpStatus.BAD_REQUEST),
	INVALID_PIN("USSD_008", "Invalid PIN provided", HttpStatus.BAD_REQUEST),
	TRANSACTION_FAILED("USSD_009", "Transaction execution failed", HttpStatus.INTERNAL_SERVER_ERROR),
	BANK_API_ERROR("USSD_010", "Bank backend communication error", HttpStatus.SERVICE_UNAVAILABLE),
	INTERNAL_ERROR("USSD_999", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
