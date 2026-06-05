package com.interswitch.service.menu;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BillsMenuHandler extends BaseMenuHandler {

    public BillsMenuHandler(MessageSource messageSource) {
        super(messageSource);
    }

    public UssdResponse showMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "BILLS_SUB");
        String menu = "Bills & Utilities\n1. Cable TV\n2. Pay Bills\n3. Check Balance\n0. Back";
        return response(menu, "/api/ussd/bills-menu", session);
    }

    public UssdResponse handleChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1": return showCableTv(request);
            case "2": return showPayBills(request);
            case "3": return showBalanceCheck(request);
            case "0": return null; // back to main menu
            default: return invalidOption(request, "/api/ussd/bills-menu");
        }
    }

    // ==================== Cable TV ====================
    private UssdResponse showCableTv(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "CABLE_TV");
        return response(msg("menu.cable.provider"), "/api/ussd/cable/provider", session);
    }

    public UssdResponse handleCableProvider(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String provider;
        switch (request.input()) {
            case "1": provider = "DSTV"; break;
            case "2": provider = "GOTV"; break;
            case "3": provider = "Startimes"; break;
            default: return invalidOption(request, "/api/ussd/cable/provider");
        }
        session.put(SessionKeys.CABLE_PROVIDER, provider);
        return response(msg("menu.cable.smartcard"), "/api/ussd/cable/smartcard", session);
    }

    public UssdResponse handleCableSmartcard(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.SMARTCARD, request.input());
        return response(msg("menu.cable.bouquet"), "/api/ussd/cable/bouquet", session);
    }

    public UssdResponse handleCableBouquet(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: map selection to amount and bouquet name
        String amount = "7500";
        session.put(SessionKeys.AMOUNT, amount);
        String text = msg("menu.cable.pin")
                .replace("{amount}", amount)
                .replace("{provider}", session.get(SessionKeys.CABLE_PROVIDER));
        return response(text, "/api/ussd/cable/pin", session);
    }

    public UssdResponse handleCablePin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch bill payment API
        String success = msg("menu.cable.success")
                .replace("{provider}", session.get(SessionKeys.CABLE_PROVIDER))
                .replace("{smartcard}", session.get(SessionKeys.SMARTCARD))
                .replace("{amount}", session.get(SessionKeys.AMOUNT));
        return endSession(success);
    }

    // ==================== Pay Bills ====================
    private UssdResponse showPayBills(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "PAY_BILLS");
        return response(msg("menu.bills.category"), "/api/ussd/bills/category", session);
    }

    public UssdResponse handleBillsCategory(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.BILL_CATEGORY, request.input());
        // TODO: fetch providers for this category
        return response(msg("menu.bills.provider"), "/api/ussd/bills/provider", session);
    }

    public UssdResponse handleBillsProvider(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.BILL_PROVIDER, request.input());
        return response(msg("menu.bills.reference"), "/api/ussd/bills/reference", session);
    }

    public UssdResponse handleBillsReference(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.BILL_REFERENCE, request.input());
        return response(msg("menu.bills.amount"), "/api/ussd/bills/amount", session);
    }

    public UssdResponse handleBillsAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.bills.pin"), "/api/ussd/bills/pin", session);
    }

    public UssdResponse handleBillsPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch bill payment API
        String success = msg("menu.bills.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{provider}", session.get(SessionKeys.BILL_PROVIDER))
                .replace("{reference}", session.get(SessionKeys.BILL_REFERENCE));
        return endSession(success);
    }

    // ==================== Balance Check ====================
    private UssdResponse showBalanceCheck(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "BALANCE");
        return response(msg("menu.balance.pin"), "/api/ussd/balance/execute", session);
    }

    public UssdResponse executeBalanceCheck(UssdRequest request) {
        // TODO: call BankOne balance enquiry
        String balanceMsg = msg("menu.balance.display")
                .replace("{balance}", "₦10,000.00")
                .replace("{account}", "123****789")
                .replace("{name}", "John Doe");
        return endSession(balanceMsg);
    }
}