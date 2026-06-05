package com.interswitch.service.menu;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityMenuHandler extends BaseMenuHandler {

    public SecurityMenuHandler(MessageSource messageSource) {
        super(messageSource);
    }

    public UssdResponse showMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "SECURITY_SUB");
        String menu = "Security & Settings\n1. PIN Management\n2. Opt In / Opt Out\n0. Back";
        return response(menu, "/api/ussd/security-menu", session);
    }

    public UssdResponse handleChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1": return showPinManagement(request);
            case "2": return showOptInOut(request);
            case "0": return null;
            default: return invalidOption(request, "/api/ussd/security-menu");
        }
    }

    // ==================== PIN Management ====================
    private UssdResponse showPinManagement(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "PIN_MANAGE");
        String menu = "PIN Management\n1. Change PIN\n2. Reset PIN\n0. Back";
        return response(menu, "/api/ussd/pin/menu", session);
    }

    public UssdResponse handlePinMenuChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1": return showChangePin(request);
            case "2": return showResetPin(request);
            case "0": return showMenu(request);
            default: return invalidOption(request, "/api/ussd/pin/menu");
        }
    }

    // --- Change PIN ---
    private UssdResponse showChangePin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "CHANGE_PIN");
        return response(msg("menu.pin.change.old"), "/api/ussd/pin/change/old", session);
    }

    public UssdResponse handleChangePinOld(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.OLD_PIN, request.input());
        return response(msg("menu.pin.change.new"), "/api/ussd/pin/change/new", session);
    }

    public UssdResponse handleChangePinNew(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.NEW_PIN, request.input());
        return response(msg("menu.pin.change.confirm"), "/api/ussd/pin/change/confirm", session);
    }

    public UssdResponse handleChangePinConfirm(UssdRequest request) {
        Map<String, String> session = getSession(request);
        if (!request.input().equals(session.get(SessionKeys.NEW_PIN))) {
            return response(msg("menu.pin.change.mismatch"), "/api/ussd/pin/change/new", session);
        }
        // TODO: call BankOne change PIN API
        return endSession(msg("menu.pin.change.success"));
    }

    // --- Reset PIN (via OTP) ---
    private UssdResponse showResetPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "RESET_PIN");
        return response(msg("menu.pin.reset.phone"), "/api/ussd/pin/reset/phone", session);
    }

    public UssdResponse handleResetPinPhone(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.PHONE_NUMBER, request.input());
        // TODO: send OTP via Interswitch
        return response(msg("menu.pin.reset.otp"), "/api/ussd/pin/reset/otp", session);
    }

    public UssdResponse handleResetPinOtp(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: validate OTP
        return response(msg("menu.pin.reset.new"), "/api/ussd/pin/reset/new", session);
    }

    public UssdResponse handleResetPinNew(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.NEW_PIN, request.input());
        return response(msg("menu.pin.reset.confirm"), "/api/ussd/pin/reset/confirm", session);
    }

    public UssdResponse handleResetPinConfirm(UssdRequest request) {
        Map<String, String> session = getSession(request);
        if (!request.input().equals(session.get(SessionKeys.NEW_PIN))) {
            return response(msg("menu.pin.change.mismatch"), "/api/ussd/pin/reset/new", session);
        }
        // TODO: call BankOne reset PIN API
        return endSession(msg("menu.pin.reset.success"));
    }

    // ==================== Opt In / Opt Out ====================
    private UssdResponse showOptInOut(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "OPT_SUB");
        String menu = "Opt In / Opt Out\n1. Opt In\n2. Opt Out\n0. Back";
        return response(menu, "/api/ussd/opt/menu", session);
    }

    public UssdResponse handleOptMenuChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1": return showOptIn(request);
            case "2": return showOptOut(request);
            case "0": return showMenu(request);
            default: return invalidOption(request, "/api/ussd/opt/menu");
        }
    }

    private UssdResponse showOptIn(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "OPT_IN");
        return response(msg("menu.optin.pin"), "/api/ussd/optin/pin", session);
    }

    public UssdResponse handleOptInPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: validate PIN
        return response(msg("menu.optin.card"), "/api/ussd/optin/card", session);
    }

    public UssdResponse handleOptInCard(UssdRequest request) {
        // TODO: update customer opt‑in status
        return endSession(msg("menu.optin.success"));
    }

    private UssdResponse showOptOut(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "OPT_OUT");
        return response(msg("menu.optout.pin"), "/api/ussd/optout/pin", session);
    }

    public UssdResponse handleOptOutPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        return response(msg("menu.optout.card"), "/api/ussd/optout/card", session);
    }

    public UssdResponse handleOptOutCard(UssdRequest request) {
        // TODO: update customer opt‑out status
        return endSession(msg("menu.optout.success"));
    }
}