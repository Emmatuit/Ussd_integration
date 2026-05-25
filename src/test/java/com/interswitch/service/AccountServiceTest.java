package com.interswitch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.interswitch.client.BankApiClient;
import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.bank.AccountDto;
import com.interswitch.exception.UssdFlowException;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private BankApiClient bankApiClient;

	@InjectMocks
	private AccountService accountService;

	@Test
	void shouldFormatAccountMenu() {
		List<AccountDto> accounts = List.of(new AccountDto("1234567890", "John Doe", "Savings", "5000.00", "NGN"),
				new AccountDto("0987654321", "John Doe", "Current", "10000.00", "NGN"));

		String menu = accountService.formatAccountMenu(accounts);

		assertThat(menu).contains("Select account:");
		assertThat(menu).contains("1.");
		assertThat(menu).contains("2.");
		assertThat(menu).contains("Savings");
		assertThat(menu).contains("Current");
		assertThat(menu).doesNotContain("1234567890");
		assertThat(menu).contains("123****890");
	}

	@Test
	void shouldGetAccountBySelection() {
		List<AccountDto> accounts = List.of(new AccountDto("1234567890", "John Doe", "Savings", "5000.00", "NGN"),
				new AccountDto("0987654321", "John Doe", "Current", "10000.00", "NGN"));

		AccountDto selected = accountService.getAccountBySelection(accounts, "2");

		assertThat(selected.accountNumber()).isEqualTo("0987654321");
	}

	@Test
	void shouldRejectInvalidSelection() {
		List<AccountDto> accounts = List.of(new AccountDto("1234567890", "John Doe", "Savings", "5000.00", "NGN"));

		assertThatThrownBy(() -> accountService.getAccountBySelection(accounts, "5"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_ACCOUNT_SELECTION);
	}

	@Test
	void shouldRejectNonNumericSelection() {
		List<AccountDto> accounts = List.of(new AccountDto("1234567890", "John Doe", "Savings", "5000.00", "NGN"));

		assertThatThrownBy(() -> accountService.getAccountBySelection(accounts, "abc"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_ACCOUNT_SELECTION);
	}

	@Test
	void shouldThrowWhenNoAccountsFound() {
		when(bankApiClient.fetchAccounts("2348012345678"))
				.thenThrow(new UssdFlowException(ErrorCode.NO_ACCOUNTS_FOUND));

		assertThatThrownBy(() -> accountService.fetchAccountsForMsisdn("2348012345678"))
				.isInstanceOf(UssdFlowException.class).extracting("errorCode").isEqualTo(ErrorCode.NO_ACCOUNTS_FOUND);
	}
}
