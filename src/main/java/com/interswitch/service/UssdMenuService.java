package com.interswitch.service;

import com.interswitch.constant.SessionKeys;
import com.interswitch.constant.TransactionStatus;
import com.interswitch.constant.UssdStep;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import com.interswitch.service.menu.AccountServicesMenuHandler;
import com.interswitch.service.menu.AirtimeDataMenuHandler;
import com.interswitch.service.menu.BillsMenuHandler;
import com.interswitch.service.menu.SecurityMenuHandler;
import com.interswitch.service.menu.TransferMenuHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UssdMenuService {

    private final MessageSource messageSource;
    private final AuditLogService auditLogService;
    private final TransferMenuHandler transferHandler;
    private final AirtimeDataMenuHandler airtimeDataHandler;
    private final BillsMenuHandler billsHandler;
    private final AccountServicesMenuHandler accountHandler;
    private final SecurityMenuHandler securityHandler;

    // ==================== MAIN MENU ====================
    public UssdResponse handleMainMenu(UssdRequest request) {
        Map<String, String> session = new HashMap<>();
        session.put(SessionKeys.FLOW, "MAIN");
        logStep(request, UssdStep.MAIN_MENU, null, msg("menu.main"), TransactionStatus.SUCCESS);
        return response(msg("menu.main"), "/api/ussd/menu-choice", session);
    }

    public UssdResponse handleMainMenuChoice(UssdRequest request) {
        String choice = request.input();
        logStep(request, UssdStep.MAIN_MENU, choice, "User selected " + choice, TransactionStatus.SUCCESS);
        switch (choice) {
            case "1": return transferHandler.showTransferMenu(request);
            case "2": return airtimeDataHandler.showMenu(request);
            case "3": return billsHandler.showMenu(request);
            case "4": return accountHandler.showMenu(request);
            case "5": return securityHandler.showMenu(request);
            case "0": return exit(request);
            default: return invalidOption(request, "/api/ussd/menu-choice");
        }
    }

    // ==================== TRANSFER DELEGATION ====================
    public UssdResponse handleTransferSubMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_MENU, request.input(), "Transfer sub-menu choice", TransactionStatus.SUCCESS);
        UssdResponse response = transferHandler.handleSubMenuChoice(request);
        return response != null ? response : handleMainMenu(request);
    }

    public UssdResponse handleTransferOthersMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_MENU, request.input(), "Transfer others menu choice", TransactionStatus.SUCCESS);
        UssdResponse response = transferHandler.handleOthersMenuChoice(request);
        return response != null ? response : transferHandler.showTransferMenu(request);
    }

    public UssdResponse handleTransferSelfAmount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_SELF_AMOUNT, request.input(), "Entered amount", TransactionStatus.SUCCESS);
        return transferHandler.handleSelfAmount(request);
    }

    public UssdResponse handleTransferSelfPin(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_SELF_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return transferHandler.handleSelfPin(request);
    }

    public UssdResponse handleTransferOtherOliveAmount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_OLIVE_AMOUNT, request.input(), "Entered amount", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherOliveAmount(request);
    }

    public UssdResponse handleTransferOtherOliveAccount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_OLIVE_ACCOUNT, request.input(), "Destination account", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherOliveAccount(request);
    }

    public UssdResponse handleTransferOtherOlivePin(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_OLIVE_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherOlivePin(request);
    }

    public UssdResponse handleTransferOtherBankAmount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_BANK_AMOUNT, request.input(), "Entered amount", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherBankAmount(request);
    }

    public UssdResponse handleTransferOtherBankAccount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_BANK_ACCOUNT, request.input(), "Destination account", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherBankAccount(request);
    }

    public UssdResponse handleTransferOtherBankSelection(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_BANK_BANK, request.input(), "Selected bank", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherBankSelection(request);
    }

    public UssdResponse handleTransferOtherBankConfirm(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_BANK_CONFIRM, "****", "PIN entered", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherBankConfirm(request);
    }

    public UssdResponse handleTransferOtherEwalletAmount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_EWALLET_AMOUNT, request.input(), "Entered amount", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherEwalletAmount(request);
    }

    public UssdResponse handleTransferOtherEwalletAccount(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_EWALLET_ACCOUNT, request.input(), "Destination account", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherEwalletAccount(request);
    }

    public UssdResponse handleTransferOtherEwalletProvider(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_EWALLET_PROVIDER, request.input(), "Selected provider", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherEwalletProvider(request);
    }

    public UssdResponse handleTransferOtherEwalletPin(UssdRequest request) {
        logStep(request, UssdStep.TRANSFER_OTHER_EWALLET_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return transferHandler.handleOtherEwalletPin(request);
    }

    // ==================== AIRTIME & DATA DELEGATION ====================
    public UssdResponse handleAirtimeDataMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_DATA_MENU, request.input(), "Airtime/data choice", TransactionStatus.SUCCESS);
        UssdResponse response = airtimeDataHandler.handleChoice(request);
        return response != null ? response : handleMainMenu(request);
    }

    public UssdResponse handleAirtimeSelfAmount(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_SELF_AMOUNT, request.input(), "Amount", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleAirtimeSelfAmount(request);
    }

    public UssdResponse handleAirtimeOthersPhone(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_OTHER_PHONE, request.input(), "Recipient phone", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleAirtimeOthersPhone(request);
    }

    public UssdResponse handleAirtimeOthersAmount(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_OTHER_AMOUNT, request.input(), "Amount", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleAirtimeOthersAmount(request);
    }

    public UssdResponse handleAirtimeOthersPin(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_OTHER_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleAirtimeOthersPin(request);
    }

    public UssdResponse handleDataMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.DATA_SELF, request.input(), "Data menu choice", TransactionStatus.SUCCESS);
        UssdResponse response = airtimeDataHandler.handleDataMenuChoice(request);
        return response != null ? response : airtimeDataHandler.showMenu(request);
    }

    public UssdResponse handleDataSelfPlan(UssdRequest request) {
        logStep(request, UssdStep.DATA_SELF_PLAN, request.input(), "Selected plan", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataSelfPlan(request);
    }

    public UssdResponse handleDataSelfPin(UssdRequest request) {
        logStep(request, UssdStep.DATA_SELF_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataSelfPin(request);
    }

    public UssdResponse handleDataOthersPhone(UssdRequest request) {
        logStep(request, UssdStep.DATA_OTHER_PHONE, request.input(), "Recipient phone", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataOthersPhone(request);
    }

    public UssdResponse handleDataOthersNetwork(UssdRequest request) {
        logStep(request, UssdStep.DATA_OTHER_NETWORK, request.input(), "Selected network", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataOthersNetwork(request);
    }

    public UssdResponse handleDataOthersPlan(UssdRequest request) {
        logStep(request, UssdStep.DATA_OTHER_PLAN, request.input(), "Selected plan", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataOthersPlan(request);
    }

    public UssdResponse handleDataOthersPin(UssdRequest request) {
        logStep(request, UssdStep.DATA_OTHER_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleDataOthersPin(request);
    }

    public UssdResponse handleAirtimeAdvancePin(UssdRequest request) {
        logStep(request, UssdStep.AIRTIME_ADVANCE_PIN, "****", "PIN entered for advance", TransactionStatus.SUCCESS);
        return airtimeDataHandler.handleAirtimeAdvancePin(request);
    }

    // ==================== BILLS & UTILITIES DELEGATION ====================
    public UssdResponse handleBillsMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.BILLS_MENU, request.input(), "Bills menu choice", TransactionStatus.SUCCESS);
        UssdResponse response = billsHandler.handleChoice(request);
        return response != null ? response : handleMainMenu(request);
    }

    public UssdResponse handleCableProvider(UssdRequest request) {
        logStep(request, UssdStep.CABLE_PROVIDER, request.input(), "Selected provider", TransactionStatus.SUCCESS);
        return billsHandler.handleCableProvider(request);
    }

    public UssdResponse handleCableSmartcard(UssdRequest request) {
        logStep(request, UssdStep.CABLE_SMARTCARD, request.input(), "Smartcard number", TransactionStatus.SUCCESS);
        return billsHandler.handleCableSmartcard(request);
    }

    public UssdResponse handleCableBouquet(UssdRequest request) {
        logStep(request, UssdStep.CABLE_BOUQUET, request.input(), "Selected bouquet", TransactionStatus.SUCCESS);
        return billsHandler.handleCableBouquet(request);
    }

    public UssdResponse handleCablePin(UssdRequest request) {
        logStep(request, UssdStep.CABLE_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return billsHandler.handleCablePin(request);
    }

    public UssdResponse handleBillsCategory(UssdRequest request) {
        logStep(request, UssdStep.BILLS_CATEGORY, request.input(), "Bill category", TransactionStatus.SUCCESS);
        return billsHandler.handleBillsCategory(request);
    }

    public UssdResponse handleBillsProvider(UssdRequest request) {
        logStep(request, UssdStep.BILLS_PROVIDER, request.input(), "Bill provider", TransactionStatus.SUCCESS);
        return billsHandler.handleBillsProvider(request);
    }

    public UssdResponse handleBillsReference(UssdRequest request) {
        logStep(request, UssdStep.BILLS_REFERENCE, request.input(), "Reference number", TransactionStatus.SUCCESS);
        return billsHandler.handleBillsReference(request);
    }

    public UssdResponse handleBillsAmount(UssdRequest request) {
        logStep(request, UssdStep.BILLS_AMOUNT, request.input(), "Amount", TransactionStatus.SUCCESS);
        return billsHandler.handleBillsAmount(request);
    }

    public UssdResponse handleBillsPin(UssdRequest request) {
        logStep(request, UssdStep.BILLS_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return billsHandler.handleBillsPin(request);
    }

    public UssdResponse executeBalanceCheck(UssdRequest request) {
        logStep(request, UssdStep.BALANCE_PIN, "****", "PIN entered for balance", TransactionStatus.SUCCESS);
        return billsHandler.executeBalanceCheck(request);
    }

    // ==================== ACCOUNT SERVICES DELEGATION ====================
    public UssdResponse handleAccountMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.ACCOUNT_SERVICES_MENU, request.input(), "Account services choice", TransactionStatus.SUCCESS);
        UssdResponse response = accountHandler.handleChoice(request);
        return response != null ? response : handleMainMenu(request);
    }

    public UssdResponse handleOpenAccountName(UssdRequest request) {
        logStep(request, UssdStep.OPEN_ACCOUNT_NAME, request.input(), "Name", TransactionStatus.SUCCESS);
        return accountHandler.handleOpenAccountName(request);
    }

    public UssdResponse handleOpenAccountDob(UssdRequest request) {
        logStep(request, UssdStep.OPEN_ACCOUNT_DOB, request.input(), "Date of birth", TransactionStatus.SUCCESS);
        return accountHandler.handleOpenAccountDob(request);
    }

    public UssdResponse handleOpenAccountPhone(UssdRequest request) {
        logStep(request, UssdStep.OPEN_ACCOUNT_PHONE, request.input(), "Phone number", TransactionStatus.SUCCESS);
        return accountHandler.handleOpenAccountPhone(request);
    }

    public UssdResponse handleOpenAccountType(UssdRequest request) {
        logStep(request, UssdStep.OPEN_ACCOUNT_TYPE, request.input(), "Account type", TransactionStatus.SUCCESS);
        return accountHandler.handleOpenAccountType(request);
    }

    public UssdResponse handleOpenAccountConfirm(UssdRequest request) {
        logStep(request, UssdStep.OPEN_ACCOUNT_CONFIRM, request.input(), "Confirmation choice", TransactionStatus.SUCCESS);
        return accountHandler.handleOpenAccountConfirm(request);
    }

    public UssdResponse handleLoanBalancePin(UssdRequest request) {
        logStep(request, UssdStep.LOAN_BALANCE_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return accountHandler.handleLoanBalancePin(request);
    }

    public UssdResponse handleTokenAmount(UssdRequest request) {
        logStep(request, UssdStep.TOKEN_AMOUNT, request.input(), "Amount", TransactionStatus.SUCCESS);
        return accountHandler.handleTokenAmount(request);
    }

    public UssdResponse handleTokenPin(UssdRequest request) {
        logStep(request, UssdStep.TOKEN_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return accountHandler.handleTokenPin(request);
    }

    // ==================== SECURITY & SETTINGS DELEGATION ====================
    public UssdResponse handleSecurityMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.SECURITY_MENU, request.input(), "Security menu choice", TransactionStatus.SUCCESS);
        UssdResponse response = securityHandler.handleChoice(request);
        return response != null ? response : handleMainMenu(request);
    }

    public UssdResponse handlePinMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.PIN_MANAGE_MENU, request.input(), "PIN management choice", TransactionStatus.SUCCESS);
        UssdResponse response = securityHandler.handlePinMenuChoice(request);
        return response != null ? response : securityHandler.showMenu(request);
    }

    public UssdResponse handleChangePinOld(UssdRequest request) {
        logStep(request, UssdStep.CHANGE_PIN_OLD, "****", "Old PIN entered", TransactionStatus.SUCCESS);
        return securityHandler.handleChangePinOld(request);
    }

    public UssdResponse handleChangePinNew(UssdRequest request) {
        logStep(request, UssdStep.CHANGE_PIN_NEW, "****", "New PIN entered", TransactionStatus.SUCCESS);
        return securityHandler.handleChangePinNew(request);
    }

    public UssdResponse handleChangePinConfirm(UssdRequest request) {
        logStep(request, UssdStep.CHANGE_PIN_CONFIRM, "****", "PIN confirmation", TransactionStatus.SUCCESS);
        return securityHandler.handleChangePinConfirm(request);
    }

    public UssdResponse handleResetPinPhone(UssdRequest request) {
        logStep(request, UssdStep.RESET_PIN_PHONE, request.input(), "Phone number", TransactionStatus.SUCCESS);
        return securityHandler.handleResetPinPhone(request);
    }

    public UssdResponse handleResetPinOtp(UssdRequest request) {
        logStep(request, UssdStep.RESET_PIN_OTP, request.input(), "OTP entered", TransactionStatus.SUCCESS);
        return securityHandler.handleResetPinOtp(request);
    }

    public UssdResponse handleResetPinNew(UssdRequest request) {
        logStep(request, UssdStep.RESET_PIN_NEW, "****", "New PIN entered", TransactionStatus.SUCCESS);
        return securityHandler.handleResetPinNew(request);
    }

    public UssdResponse handleResetPinConfirm(UssdRequest request) {
        logStep(request, UssdStep.RESET_PIN_CONFIRM, "****", "PIN confirmation", TransactionStatus.SUCCESS);
        return securityHandler.handleResetPinConfirm(request);
    }

    public UssdResponse handleOptMenuChoice(UssdRequest request) {
        logStep(request, UssdStep.OPT_IN_OUT_MENU, request.input(), "Opt in/out choice", TransactionStatus.SUCCESS);
        UssdResponse response = securityHandler.handleOptMenuChoice(request);
        return response != null ? response : securityHandler.showMenu(request);
    }

    public UssdResponse handleOptInPin(UssdRequest request) {
        logStep(request, UssdStep.OPT_IN_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return securityHandler.handleOptInPin(request);
    }

    public UssdResponse handleOptInCard(UssdRequest request) {
        logStep(request, UssdStep.OPT_IN_CARD, "******", "Card last 6 digits", TransactionStatus.SUCCESS);
        return securityHandler.handleOptInCard(request);
    }

    public UssdResponse handleOptOutPin(UssdRequest request) {
        logStep(request, UssdStep.OPT_OUT_PIN, "****", "PIN entered", TransactionStatus.SUCCESS);
        return securityHandler.handleOptOutPin(request);
    }

    public UssdResponse handleOptOutCard(UssdRequest request) {
        logStep(request, UssdStep.OPT_OUT_CARD, "******", "Card last 6 digits", TransactionStatus.SUCCESS);
        return securityHandler.handleOptOutCard(request);
    }

    // ==================== COMMON HELPERS ====================
    private UssdResponse response(String text, String callbackUrl, Map<String, String> session) {
        return UssdResponse.builder()
                .text(text)
                .callbackUrl(callbackUrl)
                .sessionData(session)
                .build();
    }

    private UssdResponse invalidOption(UssdRequest request, String callbackUrl) {
        Map<String, String> session = getSession(request);
        return response(msg("menu.invalid.option"), callbackUrl, session);
    }

    private UssdResponse exit(UssdRequest request) {
        return UssdResponse.builder()
                .text(msg("menu.exit"))
                .sessionEnd(true)
                .sessionData(new HashMap<>())
                .build();
    }

    private Map<String, String> getSession(UssdRequest request) {
        return request.sessionData() != null ? new HashMap<>(request.sessionData()) : new HashMap<>();
    }

    private String msg(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private void logStep(UssdRequest request, UssdStep step, String input, String responseText, TransactionStatus status) {
        auditLogService.logRequest(request.sessionId(), request.msisdn(), step, input, responseText, status, null);
    }
}