package com.interswitch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.interswitch.config.MockBankProperties; // ✅ fixed import
import com.interswitch.dto.response.AccountResponse;
import com.interswitch.dto.response.TransactionResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("h2")
@RequiredArgsConstructor
@Slf4j
public class MockBankService {

    private final MockBankProperties properties;
    private final MessageSource messageSource;
    private final Map<String, List<MockBankProperties.CustomerAccount>> accountsByMsisdn = new ConcurrentHashMap<>();
    private final Map<String, Double> dailyTotals = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (MockBankProperties.CustomerAccount account : properties.getAccounts()) {
            accountsByMsisdn.computeIfAbsent(account.getMsisdn(), k -> new ArrayList<>()).add(account);
        }
        log.info("Mock bank initialized with {} customer phone numbers", accountsByMsisdn.size());
    }

    public List<AccountResponse> fetchAccounts(String msisdn) {
        simulateDelay();
        List<MockBankProperties.CustomerAccount> accounts = accountsByMsisdn.getOrDefault(msisdn, List.of());
        if (accounts.isEmpty()) {
            log.warn("No accounts found for msisdn: {}", mask(msisdn));
            return List.of();
        }
        log.debug("Found {} accounts for msisdn: {}", accounts.size(), mask(msisdn));
        return accounts.stream().map(this::toAccountResponse).collect(Collectors.toList());
    }

    public TransactionResponse processAirtime(String accountNumber, String amount, String pin) {
        simulateDelay();
        MockBankProperties.CustomerAccount account = findAccount(accountNumber);
        validatePin(account, pin);
        double amountValue = parseAmount(amount);
        validateBalance(account, amountValue);
        validateDailyLimit(accountNumber, amountValue);
        account.setBalance(account.getBalance() - amountValue);
        updateDailyTotal(accountNumber, amountValue);
        String ref = "AIR-" + System.currentTimeMillis();
        log.info("Airtime: account={}, amount={}, ref={}", mask(accountNumber), amount, ref);
        return TransactionResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .status("SUCCESS")
                .message(getMessage("transaction.airtime.success"))
                .referenceNumber(ref)
                .amount(amount)
                .build();
    }

    public TransactionResponse processTransfer(String accountNumber, String amount, String pin,
                                               String destAccount, String destBank) {
        simulateDelay();
        MockBankProperties.CustomerAccount account = findAccount(accountNumber);
        validatePin(account, pin);
        double amountValue = parseAmount(amount);
        validateBalance(account, amountValue);
        validateDailyLimit(accountNumber, amountValue);
        account.setBalance(account.getBalance() - amountValue);
        updateDailyTotal(accountNumber, amountValue);
        String ref = "TRF-" + System.currentTimeMillis();
        log.info("Transfer: from={}, to={} bank={}, amount={}, ref={}",
                mask(accountNumber), mask(destAccount), destBank, amount, ref);
        return TransactionResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .status("SUCCESS")
                .message(getMessage("transaction.transfer.success"))
                .referenceNumber(ref)
                .amount(amount)
                .build();
    }

    public AccountResponse checkBalance(String accountNumber, String pin) {
        simulateDelay();
        MockBankProperties.CustomerAccount account = findAccount(accountNumber);
        validatePin(account, pin);
        return toAccountResponse(account);
    }

    public boolean changePin(String accountNumber, String oldPin, String newPin) {
        simulateDelay();
        MockBankProperties.CustomerAccount account = findAccount(accountNumber);
        validatePin(account, oldPin);
        account.setPin(newPin);
        log.info("PIN changed for account: {}", mask(accountNumber));
        return true;
    }

    private MockBankProperties.CustomerAccount findAccount(String accountNumber) {
        return accountsByMsisdn.values().stream()
                .flatMap(List::stream)
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Account not found: {}", mask(accountNumber));
                    return new IllegalArgumentException(getMessage("account.fetch.empty"));
                });
    }

    private void validatePin(MockBankProperties.CustomerAccount account, String pin) {
        String expectedPin = account.getPin() != null ? account.getPin() : properties.getDefaultPin();
        if (!expectedPin.equals(pin)) {
            log.warn("Invalid PIN for account: {}", mask(account.getAccountNumber()));
            throw new IllegalArgumentException(getMessage("transaction.invalid_pin"));
        }
    }

    private void validateBalance(MockBankProperties.CustomerAccount account, double amount) {
        if (account.getBalance() < amount) {
            log.warn("Insufficient funds: account={}, balance={}, requested={}",
                    mask(account.getAccountNumber()), account.getBalance(), amount);
            throw new IllegalArgumentException(getMessage("transaction.insufficient_funds"));
        }
    }

    private void validateDailyLimit(String accountNumber, double amount) {
        double current = dailyTotals.getOrDefault(accountNumber, 0.0);
        if (current + amount > properties.getDailyLimit()) {
            log.warn("Daily limit exceeded: account={}", mask(accountNumber));
            throw new IllegalArgumentException(getMessage("transaction.limit_exceeded"));
        }
    }

    private void updateDailyTotal(String accountNumber, double amount) {
        dailyTotals.merge(accountNumber, amount, Double::sum);
    }

    private double parseAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(getMessage("transaction.invalid_amount"));
        }
    }

    private AccountResponse toAccountResponse(MockBankProperties.CustomerAccount account) {
        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(String.format("%.2f", account.getBalance()))
                .currency(account.getCurrency())
                .build();
    }

    private String getMessage(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private void simulateDelay() {
        try {
            Thread.sleep(properties.getTransactionDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String mask(String value) {
        if (value == null || value.length() < 6) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }
}