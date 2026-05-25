package com.interswitch.exception;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.response.UssdResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	private ResponseEntity<UssdResponse> buildErrorResponse(ErrorCode errorCode) {
		UssdResponse response = UssdResponse.builder().text(truncateText(errorCode.getMessage())).sessionEnd(true)
				.sessionData(new HashMap<>()).build();

		return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
	}

	@ExceptionHandler(BankApiException.class)
	public ResponseEntity<UssdResponse> handleBankApiException(BankApiException ex) {
		log.error("Bank API exception: {}", ex.getMessage());
		return buildErrorResponse(ex.getErrorCode());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<UssdResponse> handleGenericException(Exception ex) {
		log.error("Unexpected error", ex);
		return buildErrorResponse(ErrorCode.INTERNAL_ERROR);
	}

	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<UssdResponse> handleSecurityException(CustomSecurityException ex) {
		log.warn("Security exception: {}", ex.getMessage());
		return buildErrorResponse(ex.getErrorCode());
	}

	@ExceptionHandler(UssdFlowException.class)
	public ResponseEntity<UssdResponse> handleUssdFlowException(UssdFlowException ex) {
		log.warn("USSD flow exception: {}", ex.getMessage());
		return buildErrorResponse(ex.getErrorCode());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<UssdResponse> handleValidationException(MethodArgumentNotValidException ex) {
		log.warn("Validation failed: {}", ex.getMessage());
		return buildErrorResponse(ErrorCode.INVALID_REQUEST);
	}

	private String truncateText(String text) {
		if (text != null && text.length() > 140) {
			return text.substring(0, 137) + "...";
		}
		return text;
	}
}
