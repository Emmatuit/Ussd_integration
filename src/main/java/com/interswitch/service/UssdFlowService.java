package com.interswitch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.interswitch.constant.ErrorCode;
import com.interswitch.constant.TransactionStatus;
import com.interswitch.constant.UssdStep;
import com.interswitch.dto.bank.AccountDto;
import com.interswitch.dto.bank.TransactionResultDto;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import com.interswitch.exception.UssdFlowException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UssdFlowService {

	private static final String SESSION_KEY_AMOUNT = "amount";
	private static final String SESSION_KEY_ACCOUNT_NUMBER = "accountNumber";
	private static final String SESSION_KEY_ACCOUNT_TYPE = "accountType";
	private static final String SESSION_KEY_ACCOUNTS_JSON = "accountsJson";

	private static final String CALLBACK_AIRTIME = "/api/ussd/airtime";
	private static final String CALLBACK_SELECT_ACCOUNT = "/api/ussd/select-account";
	private static final String CALLBACK_PURCHASE = "/api/ussd/purchase";

	private final AccountService accountService;
	private final TransactionService transactionService;
	private final AuditLogService auditLogService;

	private List<AccountDto> deserializeAccounts(String accountsJson) {
		if (accountsJson == null) {
			return null;
		}
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			return mapper.readValue(accountsJson,
					mapper.getTypeFactory().constructCollectionType(List.class, AccountDto.class));
		} catch (Exception e) {
			log.error("Failed to deserialize accounts", e);
			return null;
		}
	}

	private Map<String, String> getSessionData(UssdRequest request) {
		Map<String, String> sessionData = request.sessionData();
		if (sessionData == null) {
			sessionData = new HashMap<>();
		}
		return sessionData;
	}

	public UssdResponse handleAirtimeInit(UssdRequest request) {
		log.debug("Step 1 - Airtime init: sessionId={}, msisdn={}", request.sessionId(), maskMsisdn(request.msisdn()));

		String amount = request.input();

		List<AccountDto> accounts = accountService.fetchAccountsForMsisdn(request.msisdn());
		String menuText = accountService.formatAccountMenu(accounts);

		Map<String, String> sessionData = new HashMap<>();
		if (request.sessionData() != null) {
			sessionData.putAll(request.sessionData());
		}
		sessionData.put(SESSION_KEY_AMOUNT, amount);
		sessionData.put(SESSION_KEY_ACCOUNTS_JSON, serializeAccounts(accounts));

		UssdResponse response = UssdResponse.builder().text(truncate(menuText)).callbackUrl(CALLBACK_SELECT_ACCOUNT)
				.sessionData(sessionData).build();

		auditLogService.logRequest(request.sessionId(), request.msisdn(), UssdStep.AIRTIME_INIT, amount, menuText,
				TransactionStatus.SUCCESS, null);

		return response;
	}

	public UssdResponse handlePurchase(UssdRequest request) {
		log.debug("Step 3 - Purchase: sessionId={}", request.sessionId());

		Map<String, String> sessionData = getSessionData(request);

		String accountNumber = sessionData.get(SESSION_KEY_ACCOUNT_NUMBER);
		String amount = sessionData.get(SESSION_KEY_AMOUNT);
		String pin = request.input();

		if (accountNumber == null || amount == null) {
			throw new UssdFlowException(ErrorCode.INVALID_STEP, "Session expired. Please try again.");
		}

		try {
			TransactionResultDto result = transactionService.executeAirtimePurchase(accountNumber, amount, pin);

			String successText = transactionService.formatSuccessMessage(result, amount);

			UssdResponse response = UssdResponse.builder().text(truncate(successText)).sessionEnd(true)
					.sessionData(new HashMap<>()).build();

			auditLogService.logRequest(request.sessionId(), request.msisdn(), UssdStep.PURCHASE, "****", successText,
					TransactionStatus.SUCCESS, null);

			return response;

		} catch (UssdFlowException e) {
			auditLogService.logRequest(request.sessionId(), request.msisdn(), UssdStep.PURCHASE, "****", e.getMessage(),
					TransactionStatus.FAILED, e.getErrorCode());
			throw e;
		}
	}

	public UssdResponse handleSelectAccount(UssdRequest request) {
		log.debug("Step 2 - Select account: sessionId={}, input={}", request.sessionId(), request.input());

		Map<String, String> sessionData = getSessionData(request);

		List<AccountDto> accounts = deserializeAccounts(sessionData.get(SESSION_KEY_ACCOUNTS_JSON));
		if (accounts == null || accounts.isEmpty()) {
			throw new UssdFlowException(ErrorCode.INVALID_STEP, "Session expired. Please try again.");
		}

		AccountDto selectedAccount = accountService.getAccountBySelection(accounts, request.input());

		sessionData.put(SESSION_KEY_ACCOUNT_NUMBER, selectedAccount.accountNumber());
		sessionData.put(SESSION_KEY_ACCOUNT_TYPE, selectedAccount.accountType());

		String promptText = "Enter your 4-digit PIN\nto confirm purchase";

		UssdResponse response = UssdResponse.builder().text(promptText).callbackUrl(CALLBACK_PURCHASE)
				.sessionData(sessionData).build();

		auditLogService.logRequest(request.sessionId(), request.msisdn(), UssdStep.SELECT_ACCOUNT, request.input(),
				promptText, TransactionStatus.SUCCESS, null);

		return response;
	}

	private String maskMsisdn(String msisdn) {
		if (msisdn == null || msisdn.length() < 6) {
			return "****";
		}
		return msisdn.substring(0, 4) + "****" + msisdn.substring(msisdn.length() - 2);
	}

	private String serializeAccounts(List<AccountDto> accounts) {
		try {
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			return mapper.writeValueAsString(accounts);
		} catch (Exception e) {
			log.error("Failed to serialize accounts", e);
			throw new UssdFlowException(ErrorCode.INTERNAL_ERROR);
		}
	}

	private String truncate(String text) {
		if (text != null && text.length() > 140) {
			return text.substring(0, 137) + "...";
		}
		return text;
	}
}