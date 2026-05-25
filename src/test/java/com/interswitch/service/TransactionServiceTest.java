package com.interswitch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.interswitch.client.BankApiClient;
import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.bank.TransactionResultDto;
import com.interswitch.exception.UssdFlowException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private BankApiClient bankApiClient;

	@InjectMocks
	private TransactionService transactionService;

	@Test
	void shouldExecuteSuccessfulTransaction() {
		TransactionResultDto mockResult = new TransactionResultDto("TXN123", "SUCCESS", "Completed", "REF456", "1000");
		when(bankApiClient.purchaseAirtime(anyString(), anyString(), anyString())).thenReturn(mockResult);

		TransactionResultDto result = transactionService.executeAirtimePurchase("1234567890", "1000", "1234");

		assertThat(result.transactionId()).isEqualTo("TXN123");
		assertThat(result.status()).isEqualTo("SUCCESS");
	}

	@Test
	void shouldFormatSuccessMessage() {
		TransactionResultDto result = new TransactionResultDto("TXN123", "SUCCESS", "Completed", "REF456", "1000");

		String message = transactionService.formatSuccessMessage(result, "1000");

		assertThat(message).contains("Airtime purchased!");
		assertThat(message).contains("1000");
		assertThat(message).contains("REF456");
		assertThat(message.length()).isLessThanOrEqualTo(140);
	}

	@Test
	void shouldRejectEmptyAmount() {
		assertThatThrownBy(() -> transactionService.executeAirtimePurchase("1234567890", "", "1234"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	void shouldRejectInvalidPin() {
		assertThatThrownBy(() -> transactionService.executeAirtimePurchase("1234567890", "1000", "12"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_PIN);
	}

	@Test
	void shouldRejectNegativeAmount() {
		assertThatThrownBy(() -> transactionService.executeAirtimePurchase("1234567890", "-100", "1234"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	void shouldRejectNonNumericPin() {
		assertThatThrownBy(() -> transactionService.executeAirtimePurchase("1234567890", "1000", "abcd"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_PIN);
	}

	@Test
	void shouldRejectNullPin() {
		assertThatThrownBy(() -> transactionService.executeAirtimePurchase("1234567890", "1000", null))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.INVALID_PIN);
	}
}
