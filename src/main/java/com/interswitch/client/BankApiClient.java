package com.interswitch.client;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.interswitch.constant.ErrorCode;
import com.interswitch.dto.bank.AccountDto;
import com.interswitch.dto.bank.TransactionResultDto;
import com.interswitch.exception.BankApiException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BankApiClient {

    private final RestClient bankRestClient;
    private final RestClient stagingRestClient;

    public BankApiClient(
            RestClient bankRestClient,
            @Value("${staging.api.base-url:https://staging.mybankone.com}") String stagingBaseUrl) {
        this.bankRestClient = bankRestClient;
        this.stagingRestClient = RestClient.builder().baseUrl(stagingBaseUrl).build();
    }

    @Value("${staging.api.bills-payment-path:/ThirdPartyAPIService/APIService/BillsPayment}")
    private String billsPaymentPath;

    @Value("${staging.api.transfer-path:/thirdpartyapiservice/apiservice/Transfer}")
    private String transferPath;

    // ==================== MOCK BANK / REAL BANK API ====================

    public List<AccountDto> fetchAccounts(String msisdn) {
        log.debug("Fetching accounts for msisdn: {}", maskMsisdn(msisdn));
        try {
            AccountDto[] accounts = bankRestClient.get().uri("/api/accounts/{msisdn}", msisdn).retrieve()
                    .body(AccountDto[].class);

            if (accounts == null || accounts.length == 0) {
                throw new BankApiException(ErrorCode.NO_ACCOUNTS_FOUND);
            }

            log.debug("Found {} accounts for msisdn: {}", accounts.length, maskMsisdn(msisdn));
            return List.of(accounts);
        } catch (BankApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch accounts for msisdn: {}", maskMsisdn(msisdn), e);
            throw new BankApiException(ErrorCode.BANK_API_ERROR, "Failed to fetch accounts");
        }
    }

    public TransactionResultDto purchaseAirtime(String accountNumber, String amount, String pin) {
        log.debug("Processing airtime purchase: account={}, amount={}", maskAccount(accountNumber), amount);
        try {
            AirtimeRequestDto requestBody = new AirtimeRequestDto(accountNumber, amount, pin);

            TransactionResultDto result = bankRestClient.post().uri("/api/transactions/airtime").body(requestBody)
                    .retrieve().body(TransactionResultDto.class);

            if (result == null) {
                throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Empty response from bank");
            }

            log.debug("Airtime purchase result: transactionId={}, status={}", result.transactionId(), result.status());
            return result;
        } catch (BankApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process airtime purchase", e);
            throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Transaction could not be completed");
        }
    }

    public TransactionResultDto transfer(String accountNumber, String amount, String pin, String destAccount, String destBank) {
        log.debug("Processing transfer: from={}, to={}, amount={}", maskAccount(accountNumber), maskAccount(destAccount), amount);
        try {
            Map<String, String> body = Map.of(
                    "accountNumber", accountNumber,
                    "amount", amount,
                    "pin", pin,
                    "destAccount", destAccount,
                    "destBank", destBank
            );

            TransactionResultDto result = bankRestClient.post()
                    .uri("/api/transactions/transfer")
                    .body(body)
                    .retrieve()
                    .body(TransactionResultDto.class);

            if (result == null) {
                throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Empty response from bank");
            }
            return result;
        } catch (BankApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to process transfer", e);
            throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Transfer could not be completed");
        }
    }

    public AccountDto checkBalance(String accountNumber, String pin) {
        log.debug("Checking balance: account={}", maskAccount(accountNumber));
        try {
            Map<String, String> body = Map.of("accountNumber", accountNumber, "pin", pin);

            AccountDto result = bankRestClient.post()
                    .uri("/api/transactions/balance")
                    .body(body)
                    .retrieve()
                    .body(AccountDto.class);

            if (result == null) {
                throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Empty response from bank");
            }
            return result;
        } catch (BankApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to check balance", e);
            throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "Balance enquiry failed");
        }
    }

    public boolean changePin(String accountNumber, String oldPin, String newPin) {
        log.debug("Changing PIN: account={}", maskAccount(accountNumber));
        try {
            Map<String, String> body = Map.of("accountNumber", accountNumber, "oldPin", oldPin, "newPin", newPin);

            bankRestClient.post()
                    .uri("/api/transactions/change-pin")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.error("Failed to change PIN", e);
            throw new BankApiException(ErrorCode.TRANSACTION_FAILED, "PIN change failed");
        }
    }

    // ==================== STAGING BANK API CALLS ====================

    public List<Map<String, Object>> getCommercialBanks(String token) {
        log.info("Fetching commercial banks from staging API");
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> banks = (List<Map<String, Object>>) stagingRestClient.get()
                    .uri(billsPaymentPath + "/GetCommercialBanks/{token}", token)
                    .retrieve()
                    .body(List.class);

            if (banks == null || banks.isEmpty()) {
                log.warn("No banks returned from staging API");
                return List.of();
            }

            log.info("Fetched {} banks total", banks.size());
            return banks;
        } catch (Exception e) {
            log.error("Failed to fetch commercial banks", e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> nameEnquiry(String accountNumber, String bankCode, String token) {
        log.info("Name enquiry: account={}, bank={}", maskAccount(accountNumber), bankCode);
        try {
            Map<String, Object> requestBody = Map.of(
                    "AccountNumber", accountNumber,
                    "BankCode", bankCode,
                    "Token", token
            );

            Map<String, Object> response = stagingRestClient.post()
                    .uri(transferPath + "/NameEnquiry")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            log.info("Name enquiry response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Name enquiry failed", e);
            return null;
        }
    }

    // ==================== HELPERS ====================

    private String maskMsisdn(String msisdn) {
        if (msisdn == null || msisdn.length() < 6) return "****";
        return msisdn.substring(0, 4) + "****" + msisdn.substring(msisdn.length() - 2);
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 6) return "****";
        return accountNumber.substring(0, 3) + "****" + accountNumber.substring(accountNumber.length() - 3);
    }

    private record AirtimeRequestDto(
            @com.fasterxml.jackson.annotation.JsonProperty("accountNumber") String accountNumber,
            @com.fasterxml.jackson.annotation.JsonProperty("amount") String amount,
            @com.fasterxml.jackson.annotation.JsonProperty("pin") String pin) {
    }
}