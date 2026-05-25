package com.interswitch.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.response.UssdResponse;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldHandleBankApiException() {
		BankApiException ex = new BankApiException(ErrorCode.BANK_API_ERROR);

		ResponseEntity<UssdResponse> response = handler.handleBankApiException(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void shouldHandleGenericExceptionAsInternalError() {
		Exception ex = new RuntimeException("Something broke");

		ResponseEntity<UssdResponse> response = handler.handleGenericException(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().text()).contains("unexpected error");
	}

	@Test
	void shouldHandleSecurityExceptionWithCorrectStatus() {
		CustomSecurityException ex = new CustomSecurityException(ErrorCode.INVALID_SIGNATURE);

		ResponseEntity<UssdResponse> response = handler.handleSecurityException(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().text()).contains("Invalid request signature");
		assertThat(response.getBody().sessionEnd()).isTrue();
	}

	@Test
	void shouldHandleUssdFlowException() {
		UssdFlowException ex = new UssdFlowException(ErrorCode.INVALID_PIN);

		ResponseEntity<UssdResponse> response = handler.handleUssdFlowException(ex);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().text()).contains("Invalid PIN");
	}

	@Test
	void shouldTruncateLongErrorMessages() {
		String longMessage = "A".repeat(200);
		CustomSecurityException ex = new CustomSecurityException(ErrorCode.INVALID_SIGNATURE, longMessage);

		ResponseEntity<UssdResponse> response = handler.handleSecurityException(ex);

		assertThat(response.getBody().text()).hasSizeLessThanOrEqualTo(140);
	}
}