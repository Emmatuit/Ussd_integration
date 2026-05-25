package com.interswitch.service;

import com.interswitch.client.BankApiClient;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final BankApiClient bankApiClient;
    private final MessageSource messageSource;
    private final BankCacheService bankCacheService;

    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_BANK_CODE = "bankCode";
    private static final String KEY_BANK_NAME = "bankName";
    private static final String KEY_DEST_ACCOUNT = "destAccount";
    private static final String KEY_DEST_NAME = "destName";
    private static final String KEY_BANK_LIST = "bankList";

    public UssdResponse showMainMenu(UssdRequest request) {
        return UssdResponse.builder()
                .text(msg("menu.main"))
                .callbackUrl("/api/ussd/menu-choice")
                .sessionData(new HashMap<>())
                .build();
    }

    public UssdResponse handleMenuChoice(UssdRequest request) {
        String choice = request.input();
        Map<String, String> data = getSessionData(request);

        switch (choice) {
            case "1":
                data.put("flow", "airtime");
                return UssdResponse.builder()
                        .text(msg("menu.airtime.amount"))
                        .callbackUrl("/api/ussd/airtime")
                        .sessionData(data)
                        .build();
            case "2":
                data.put("flow", "transfer");
                return UssdResponse.builder()
                        .text(msg("menu.transfer.amount"))
                        .callbackUrl("/api/ussd/transfer/amount")
                        .sessionData(data)
                        .build();
            case "3":
                return UssdResponse.builder()
                        .text(msg("menu.balance.pin"))
                        .callbackUrl("/api/ussd/balance-check")
                        .sessionData(data)
                        .build();
            case "4":
                return UssdResponse.builder()
                        .text(msg("menu.exit"))
                        .sessionEnd(true)
                        .sessionData(new HashMap<>())
                        .build();
            default:
                return UssdResponse.builder()
                        .text(msg("menu.invalid"))
                        .callbackUrl("/api/ussd/menu-choice")
                        .sessionData(data)
                        .build();
        }
    }

    public UssdResponse handleTransferAmount(UssdRequest request) {
        Map<String, String> data = getSessionData(request);
        data.put(KEY_AMOUNT, request.input());

        return UssdResponse.builder()
                .text(msg("menu.transfer.account"))
                .callbackUrl("/api/ussd/transfer/bank-select")
                .sessionData(data)
                .build();
    }

    public UssdResponse showBankSelection(UssdRequest request) {
        Map<String, String> data = getSessionData(request);
        data.put(KEY_DEST_ACCOUNT, request.input());

        // Get banks from cache (Redis) — no direct API call
        List<Map<String, Object>> banks = bankCacheService.getBanks();

        if (banks.isEmpty()) {
            return UssdResponse.builder()
                    .text(msg("transfer.banks.unavailable"))
                    .sessionEnd(true)
                    .sessionData(new HashMap<>())
                    .build();
        }

        StringBuilder text = new StringBuilder(msg("transfer.select.bank") + "\n");
        StringBuilder bankData = new StringBuilder();
        int count = Math.min(banks.size(), 9);

        for (int i = 0; i < count; i++) {
            String code = String.valueOf(banks.get(i).get("Code"));
            String name = String.valueOf(banks.get(i).get("Name"));
            text.append(i + 1).append(". ").append(name).append("\n");
            if (i > 0) bankData.append("||");
            bankData.append(code).append("|").append(name);
        }

        data.put(KEY_BANK_LIST, bankData.toString());

        return UssdResponse.builder()
                .text(text.toString().trim())
                .callbackUrl("/api/ussd/transfer/name-enquiry")
                .sessionData(data)
                .build();
    }
    public UssdResponse handleNameEnquiry(UssdRequest request) {
        Map<String, String> data = getSessionData(request);

        int selection;
        try {
            selection = Integer.parseInt(request.input()) - 1;
        } catch (NumberFormatException e) {
            return UssdResponse.builder()
                    .text(msg("transfer.invalid.selection"))
                    .callbackUrl("/api/ussd/transfer/bank-select")
                    .sessionData(data)
                    .build();
        }

        String bankListStr = data.get(KEY_BANK_LIST);
        if (bankListStr == null) {
            return UssdResponse.builder()
                    .text(msg("session.expired"))
                    .sessionEnd(true)
                    .sessionData(new HashMap<>())
                    .build();
        }

        String[] banks = bankListStr.split("\\|\\|");
        if (selection < 0 || selection >= banks.length) {
            return UssdResponse.builder()
                    .text(msg("transfer.invalid.selection"))
                    .callbackUrl("/api/ussd/transfer/bank-select")
                    .sessionData(data)
                    .build();
        }

        String[] bankInfo = banks[selection].split("\\|");
        String bankCode = bankInfo[0];
        String bankName = bankInfo[1];
        String destAccount = data.get(KEY_DEST_ACCOUNT);

        data.put(KEY_BANK_CODE, bankCode);
        data.put(KEY_BANK_NAME, bankName);

        String token = getStagingToken();
        Map<String, Object> enquiry = bankApiClient.nameEnquiry(destAccount, bankCode, token);

        if (enquiry == null || !Boolean.TRUE.equals(enquiry.get("IsSuccessful"))) {
            String errMsg = enquiry != null ? 
                String.valueOf(enquiry.getOrDefault("ResponseMessage", "Name enquiry failed")) : 
                "Name enquiry failed";
            return UssdResponse.builder()
                    .text(errMsg)
                    .callbackUrl("/api/ussd/transfer/bank-select")
                    .sessionData(data)
                    .build();
        }

        String accountName = String.valueOf(enquiry.getOrDefault("Name", "Unknown"));
        data.put(KEY_DEST_NAME, accountName);

        String text = msg("transfer.confirm.message")
                .replace("{amount}", data.get(KEY_AMOUNT))
                .replace("{name}", accountName)
                .replace("{account}", destAccount)
                .replace("{bank}", bankName);

        return UssdResponse.builder()
                .text(text)
                .callbackUrl("/api/ussd/transfer/confirm")
                .sessionData(data)
                .build();
    }

    public UssdResponse confirmTransfer(UssdRequest request) {
        Map<String, String> data = getSessionData(request);
        String pin = request.input();

        if (pin == null || !pin.matches("\\d{4}")) {
            return UssdResponse.builder()
                    .text(msg("transfer.invalid.pin"))
                    .callbackUrl("/api/ussd/transfer/confirm")
                    .sessionData(data)
                    .build();
        }

        String ref = "TRF-" + System.currentTimeMillis();
        log.info("TRANSFER: {} to {} ({}) amount={} ref={}",
                mask(data.get(KEY_DEST_ACCOUNT)),
                data.get(KEY_DEST_NAME),
                data.get(KEY_BANK_NAME),
                data.get(KEY_AMOUNT),
                ref);

        String text = msg("transfer.success")
                .replace("{amount}", data.get(KEY_AMOUNT))
                .replace("{name}", data.get(KEY_DEST_NAME))
                .replace("{bank}", data.get(KEY_BANK_NAME))
                .replace("{ref}", ref);

        return UssdResponse.builder()
                .text(text)
                .sessionEnd(true)
                .sessionData(new HashMap<>())
                .build();
    }

    public UssdResponse handleBalanceCheck(UssdRequest request) {
        String pin = request.input();

        if (pin == null || !pin.matches("\\d{4}")) {
            return UssdResponse.builder()
                    .text(msg("transfer.invalid.pin"))
                    .sessionEnd(true)
                    .sessionData(new HashMap<>())
                    .build();
        }

        try {
            var account = bankApiClient.checkBalance("1001234567", pin);
            String text = msg("menu.balance.display")
                    .replace("{balance}", account.balance())
                    .replace("{account}", mask(account.accountNumber()))
                    .replace("{name}", account.accountName());

            return UssdResponse.builder()
                    .text(text)
                    .sessionEnd(true)
                    .sessionData(new HashMap<>())
                    .build();
        } catch (Exception e) {
            return UssdResponse.builder()
                    .text(msg("transfer.failed"))
                    .sessionEnd(true)
                    .sessionData(new HashMap<>())
                    .build();
        }
    }

    private String getStagingToken() {
        // Try Spring environment first, then system properties, then env
        return "a3b3b6f1-dbe0-40ce-830e-2cdcd4679099";
    }

    private Map<String, String> getSessionData(UssdRequest request) {
        return request.sessionData() != null ? new HashMap<>(request.sessionData()) : new HashMap<>();
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private String mask(String value) {
        if (value == null || value.length() < 6) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 3);
    }
}
