package com.interswitch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.interswitch.client.BankApiClient;
import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.bank.AccountDto;
import com.interswitch.exception.UssdFlowException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

	private final BankApiClient bankApiClient;

	public List<AccountDto> fetchAccountsForMsisdn(String msisdn) {
		List<AccountDto> accounts = bankApiClient.fetchAccounts(msisdn);

		if (accounts.isEmpty()) {
			throw new UssdFlowException(ErrorCode.NO_ACCOUNTS_FOUND);
		}

		return accounts;
	}

	public String formatAccountMenu(List<AccountDto> accounts) {
		StringBuilder menu = new StringBuilder("Select account:\n");

		for (int i = 0; i < accounts.size(); i++) {
			AccountDto account = accounts.get(i);
			String maskedAccount = maskAccount(account.accountNumber());
			menu.append(i + 1).append(". ").append(maskedAccount).append(" (")
					.append(account.accountType() != null ? account.accountType() : "Savings").append(")");

			if (i < accounts.size() - 1) {
				menu.append("\n");
			}
		}

		return menu.toString();
	}

	public AccountDto getAccountBySelection(List<AccountDto> accounts, String selectionInput) {
		try {
			int index = Integer.parseInt(selectionInput) - 1;

			if (index < 0 || index >= accounts.size()) {
				throw new UssdFlowException(ErrorCode.INVALID_ACCOUNT_SELECTION,
						"Invalid selection. Please enter 1 to " + accounts.size());
			}

			return accounts.get(index);
		} catch (NumberFormatException e) {
			throw new UssdFlowException(ErrorCode.INVALID_ACCOUNT_SELECTION, "Please enter a valid number");
		}
	}

	private String maskAccount(String accountNumber) {
		if (accountNumber == null || accountNumber.length() < 6) {
			return "****";
		}
		return accountNumber.substring(0, 3) + "****" + accountNumber.substring(accountNumber.length() - 3);
	}
}