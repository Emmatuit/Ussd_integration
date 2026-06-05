package com.interswitch.service.menu;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AccountServicesMenuHandler extends BaseMenuHandler {

    public AccountServicesMenuHandler(MessageSource messageSource) {
        super(messageSource);
    }

    public UssdResponse showMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "ACCOUNT_SUB");
        String menu = "Account Services\n1. Open Account\n2. Loan Balance\n3. Generate Token\n0. Back";
        return response(menu, "/api/ussd/account-menu", session);
    }

    public UssdResponse handleChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1": return showOpenAccount(request);
            case "2": return showLoanBalance(request);
            case "3": return showGenerateToken(request);
            case "0": return null;
            default: return invalidOption(request, "/api/ussd/account-menu");
        }
    }

    // ==================== Open Account ====================
    private UssdResponse showOpenAccount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "OPEN_ACCOUNT");
        return response(msg("menu.openaccount.name"), "/api/ussd/openaccount/name", session);
    }

    public UssdResponse handleOpenAccountName(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.OPEN_ACCOUNT_NAME, request.input());
        return response(msg("menu.openaccount.dob"), "/api/ussd/openaccount/dob", session);
    }

    public UssdResponse handleOpenAccountDob(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.OPEN_ACCOUNT_DOB, request.input());
        return response(msg("menu.openaccount.phone"), "/api/ussd/openaccount/phone", session);
    }

    public UssdResponse handleOpenAccountPhone(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.OPEN_ACCOUNT_PHONE, request.input());
        return response(msg("menu.openaccount.type"), "/api/ussd/openaccount/type", session);
    }

    public UssdResponse handleOpenAccountType(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String type = request.input().equals("1") ? "Savings" : "Current";
        session.put(SessionKeys.OPEN_ACCOUNT_TYPE, type);
        String confirmMsg = msg("menu.openaccount.confirm")
                .replace("{name}", session.get(SessionKeys.OPEN_ACCOUNT_NAME))
                .replace("{dob}", session.get(SessionKeys.OPEN_ACCOUNT_DOB))
                .replace("{phone}", session.get(SessionKeys.OPEN_ACCOUNT_PHONE))
                .replace("{type}", type);
        return response(confirmMsg, "/api/ussd/openaccount/confirm", session);
    }

    public UssdResponse handleOpenAccountConfirm(UssdRequest request) {
        if (!"1".equals(request.input())) {
            return endSession(msg("menu.cancel"));
        }
        // TODO: call BankOne account opening API
        Map<String, String> session = getSession(request);
        String successMsg = msg("menu.openaccount.success")
                .replace("{accountNumber}", "0123456789")
                .replace("{phone}", session.get(SessionKeys.OPEN_ACCOUNT_PHONE));
        return endSession(successMsg);
    }

    // ==================== Loan Balance ====================
    private UssdResponse showLoanBalance(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "LOAN_BALANCE");
        return response(msg("menu.loan.balance.pin"), "/api/ussd/loan/balance/pin", session);
    }

    public UssdResponse handleLoanBalancePin(UssdRequest request) {
        // TODO: call BankOne loan enquiry
        String loanMsg = msg("menu.loan.balance.display").replace("{balance}", "₦50,000.00");
        return endSession(loanMsg);
    }

    // ==================== Generate Token ====================
    private UssdResponse showGenerateToken(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "GENERATE_TOKEN");
        return response(msg("menu.token.amount"), "/api/ussd/token/amount", session);
    }

    public UssdResponse handleTokenAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.token.pin"), "/api/ussd/token/pin", session);
    }

    public UssdResponse handleTokenPin(UssdRequest request) {
        // TODO: generate token via Interswitch OTP API or BankOne
        String token = "123456";
        String successMsg = msg("menu.token.success").replace("{token}", token);
        return endSession(successMsg);
    }
}