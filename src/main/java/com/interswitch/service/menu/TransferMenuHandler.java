package com.interswitch.service.menu;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;

import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class TransferMenuHandler extends BaseMenuHandler {

    public TransferMenuHandler(MessageSource messageSource) {
        super(messageSource);
    }

    public UssdResponse showTransferMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "TRANSFER_SUB");
        return response(msg("menu.transfer.type"), "/api/ussd/transfer/sub-menu", session);
    }

    public UssdResponse handleSubMenuChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1":
                session.put(SessionKeys.FLOW, "TRANSFER_SELF");
                return response(msg("menu.transfer.self.amount"), "/api/ussd/transfer/self/amount", session);
            case "2":
                session.put(SessionKeys.FLOW, "TRANSFER_OTHERS_CHOOSE");
                return response(msg("menu.transfer.others.choose"), "/api/ussd/transfer/others-menu", session);
            case "0":
                return null; // go back to main menu (handled by caller)
            default:
                return invalidOption(request, "/api/ussd/transfer/sub-menu");
        }
    }

    public UssdResponse handleOthersMenuChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1":
                session.put(SessionKeys.FLOW, "TRANSFER_OTHER_OLIVE");
                return response(msg("menu.transfer.others.olive.amount"), "/api/ussd/transfer/other-olive/amount", session);
            case "2":
                session.put(SessionKeys.FLOW, "TRANSFER_OTHER_BANK");
                return response(msg("menu.transfer.others.bank.amount"), "/api/ussd/transfer/other-bank/amount", session);
            case "3":
                session.put(SessionKeys.FLOW, "TRANSFER_OTHER_EWALLET");
                return response(msg("menu.transfer.others.ewallet.amount"), "/api/ussd/transfer/other-ewallet/amount", session);
            case "0":
                return showTransferMenu(request);
            default:
                return invalidOption(request, "/api/ussd/transfer/others-menu");
        }
    }

    // Self transfer
    public UssdResponse handleSelfAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.transfer.self.pin"), "/api/ussd/transfer/self/pin", session);
    }

    public UssdResponse handleSelfPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call BankOne
        String success = msg("menu.transfer.self.success").replace("{amount}", session.get(SessionKeys.AMOUNT));
        return endSession(success);
    }

    // Transfer to other Olive account
    public UssdResponse handleOtherOliveAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.transfer.others.olive.account"), "/api/ussd/transfer/other-olive/account", session);
    }

    public UssdResponse handleOtherOliveAccount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.DEST_ACCOUNT, request.input());
        return response(msg("menu.transfer.others.olive.pin"), "/api/ussd/transfer/other-olive/pin", session);
    }

    public UssdResponse handleOtherOlivePin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String success = msg("menu.transfer.others.olive.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{account}", session.get(SessionKeys.DEST_ACCOUNT));
        return endSession(success);
    }

    // Transfer to other bank (inter-bank)
    public UssdResponse handleOtherBankAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.transfer.others.bank.account"), "/api/ussd/transfer/other-bank/account", session);
    }

    public UssdResponse handleOtherBankAccount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.DEST_ACCOUNT, request.input());
        // TODO: fetch bank list from cache
        String bankMenu = "Select bank:\n1. Access\n2. GTBank\n3. FirstBank\n0. Back";
        return response(bankMenu, "/api/ussd/transfer/other-bank/bank", session);
    }

    public UssdResponse handleOtherBankSelection(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: map selection to bank code and name
        session.put(SessionKeys.BANK_CODE, "001");
        session.put(SessionKeys.BANK_NAME, "Access Bank");
        // TODO: call name enquiry
        session.put(SessionKeys.DEST_NAME, "John Doe");
        String confirmMsg = msg("menu.transfer.others.bank.nameenquiry").replace("{name}", session.get(SessionKeys.DEST_NAME));
        return response(confirmMsg, "/api/ussd/transfer/other-bank/confirm", session);
    }

    public UssdResponse handleOtherBankConfirm(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch single transfer
        String success = msg("menu.transfer.others.bank.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{name}", session.get(SessionKeys.DEST_NAME))
                .replace("{account}", session.get(SessionKeys.DEST_ACCOUNT))
                .replace("{bank}", session.get(SessionKeys.BANK_NAME))
                .replace("{ref}", "REF123456");
        return endSession(success);
    }

    // Transfer to eWallet (OPAY, PalmPay, etc.)
    public UssdResponse handleOtherEwalletAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.transfer.others.ewallet.account"), "/api/ussd/transfer/other-ewallet/account", session);
    }

    public UssdResponse handleOtherEwalletAccount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.DEST_ACCOUNT, request.input());
        return response(msg("menu.transfer.others.ewallet.provider"), "/api/ussd/transfer/other-ewallet/provider", session);
    }

    public UssdResponse handleOtherEwalletProvider(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String provider;
        switch (request.input()) {
            case "1": provider = "OPAY"; break;
            case "2": provider = "PalmPay"; break;
            case "3": provider = "Moniepoint"; break;
            case "4": provider = "Paga"; break;
            default: return invalidOption(request, "/api/ussd/transfer/other-ewallet/provider");
        }
        session.put(SessionKeys.EWALLET_PROVIDER, provider);
        return response(msg("menu.transfer.others.ewallet.pin"), "/api/ussd/transfer/other-ewallet/pin", session);
    }

    public UssdResponse handleOtherEwalletPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch wallet payment API
        String success = msg("menu.transfer.others.ewallet.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{provider}", session.get(SessionKeys.EWALLET_PROVIDER))
                .replace("{account}", session.get(SessionKeys.DEST_ACCOUNT));
        return endSession(success);
    }
}