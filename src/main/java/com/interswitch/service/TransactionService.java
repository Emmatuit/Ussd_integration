package com.interswitch.service;

import org.springframework.stereotype.Service;

import com.interswitch.client.BankApiClient;
import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.bank.TransactionResultDto;
import com.interswitch.exception.UssdFlowException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

	private static final String PIN_PATTERN = "\\d{4}";

	private final BankApiClient bankApiClient;

	public TransactionResultDto executeAirtimePurchase(String accountNumber, String amount, String pin) {
		validatePin(pin);
		validateAmount(amount);

		TransactionResultDto result = bankApiClient.purchaseAirtime(accountNumber, amount, pin);

		if (result == null || !"SUCCESS".equalsIgnoreCase(result.status())) {
			throw new UssdFlowException(ErrorCode.TRANSACTION_FAILED,
					result != null ? result.message() : "Transaction failed");
		}

		return result;
	}

	public String formatSuccessMessage(TransactionResultDto result, String amount) {
		return "Airtime purchased!\n" + "Amt: " + amount + "\n" + "Ref: " + result.referenceNumber();
	}

	private void validateAmount(String amount) {
		if (amount == null || amount.isEmpty()) {
			throw new UssdFlowException(ErrorCode.INVALID_REQUEST, "Amount is required");
		}
		try {
			double value = Double.parseDouble(amount);
			if (value <= 0) {
				throw new UssdFlowException(ErrorCode.INVALID_REQUEST, "Amount must be greater than zero");
			}
		} catch (NumberFormatException e) {
			throw new UssdFlowException(ErrorCode.INVALID_REQUEST, "Invalid amount format");
		}
	}

	private void validatePin(String pin) {
		if (pin == null || !pin.matches(PIN_PATTERN)) {
			throw new UssdFlowException(ErrorCode.INVALID_PIN, "PIN must be exactly 4 digits");
		}
	}
}