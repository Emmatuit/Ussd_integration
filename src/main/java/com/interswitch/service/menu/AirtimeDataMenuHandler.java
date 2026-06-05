package com.interswitch.service.menu;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AirtimeDataMenuHandler extends BaseMenuHandler {

    public AirtimeDataMenuHandler(MessageSource messageSource) {
        super(messageSource);
    }

    public UssdResponse showMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "AIRDATA_SUB");
        String menu = msg("menu.airtime.data"); // key added below
        return response(menu, "/api/ussd/airdata-menu", session);
    }

    public UssdResponse handleChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1":
                session.put(SessionKeys.FLOW, "AIRTIME_SELF");
                return response(msg("menu.airtime.self.amount"), "/api/ussd/airtime/self/amount", session);
            case "2":
                session.put(SessionKeys.FLOW, "AIRTIME_OTHER");
                return response(msg("menu.airtime.others.phone"), "/api/ussd/airtime/others/phone", session);
            case "3":
                return showDataMenu(request);
            case "4":
                session.put(SessionKeys.FLOW, "AIRTIME_ADVANCE");
                return response(msg("menu.airtime.advance.eligibility"), "/api/ussd/airtime/advance/pin", session);
            case "0":
                return null; // back to main menu
            default:
                return invalidOption(request, "/api/ussd/airdata-menu");
        }
    }

    // ==================== Airtime Self ====================
    public UssdResponse handleAirtimeSelfAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        // TODO: call Interswitch purchase API with saved card/token
        String successMsg = msg("menu.airtime.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{phone}", request.msisdn());
        return endSession(successMsg);
    }

    // ==================== Airtime Others ====================
    public UssdResponse handleAirtimeOthersPhone(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.PHONE_NUMBER, request.input());
        return response(msg("menu.airtime.others.amount"), "/api/ussd/airtime/others/amount", session);
    }

    public UssdResponse handleAirtimeOthersAmount(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.AMOUNT, request.input());
        return response(msg("menu.airtime.others.pin"), "/api/ussd/airtime/others/pin", session);
    }

    public UssdResponse handleAirtimeOthersPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch purchase API with saved card/token
        String successMsg = msg("menu.airtime.success")
                .replace("{amount}", session.get(SessionKeys.AMOUNT))
                .replace("{phone}", session.get(SessionKeys.PHONE_NUMBER));
        return endSession(successMsg);
    }

    // ==================== Data Menu ====================
    private UssdResponse showDataMenu(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.FLOW, "DATA_SUB");
        return response(msg("menu.data.self.choose"), "/api/ussd/data/menu", session);
    }

    public UssdResponse handleDataMenuChoice(UssdRequest request) {
        Map<String, String> session = getSession(request);
        String choice = request.input();
        switch (choice) {
            case "1":
                session.put(SessionKeys.FLOW, "DATA_SELF");
                return response(msg("menu.data.self.plan"), "/api/ussd/data/self/plan", session);
            case "2":
                session.put(SessionKeys.FLOW, "DATA_OTHER");
                return response(msg("menu.data.others.phone"), "/api/ussd/data/others/phone", session);
            case "0":
                return showMenu(request);
            default:
                return invalidOption(request, "/api/ussd/data/menu");
        }
    }

    // ----- Data Self -----
    public UssdResponse handleDataSelfPlan(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: map selection to actual data plan (e.g., 1GB, 2GB, etc.)
        // For now, store a placeholder
        String plan;
        switch (request.input()) {
            case "1": plan = "1GB - ₦500"; break;
            case "2": plan = "2GB - ₦1000"; break;
            case "3": plan = "5GB - ₦2000"; break;
            default: return invalidOption(request, "/api/ussd/data/self/plan");
        }
        session.put(SessionKeys.DATA_PLAN, plan);
        String prompt = msg("menu.data.self.pin").replace("{plan}", plan);
        return response(prompt, "/api/ussd/data/self/pin", session);
    }

    public UssdResponse handleDataSelfPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch bill payment / data API
        String successMsg = msg("menu.data.self.success")
                .replace("{plan}", session.get(SessionKeys.DATA_PLAN))
                .replace("{phone}", request.msisdn());
        return endSession(successMsg);
    }

    // ----- Data Other -----
    public UssdResponse handleDataOthersPhone(UssdRequest request) {
        Map<String, String> session = getSession(request);
        session.put(SessionKeys.PHONE_NUMBER, request.input());
        return response(msg("menu.data.others.network"), "/api/ussd/data/others/network", session);
    }

    public UssdResponse handleDataOthersNetwork(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // Store network choice (1=MTN, 2=Airtel, etc.)
        session.put(SessionKeys.DATA_NETWORK, request.input());
        return response(msg("menu.data.others.plan"), "/api/ussd/data/others/plan", session);
    }

    public UssdResponse handleDataOthersPlan(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: map selection to actual data plan
        String plan;
        switch (request.input()) {
            case "1": plan = "1GB - ₦500"; break;
            case "2": plan = "2GB - ₦1000"; break;
            default: return invalidOption(request, "/api/ussd/data/others/plan");
        }
        session.put(SessionKeys.DATA_PLAN, plan);
        return response(msg("menu.data.others.pin"), "/api/ussd/data/others/pin", session);
    }

    public UssdResponse handleDataOthersPin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: call Interswitch bill payment / data API
        String successMsg = msg("menu.data.others.success")
                .replace("{plan}", session.get(SessionKeys.DATA_PLAN))
                .replace("{phone}", session.get(SessionKeys.PHONE_NUMBER));
        return endSession(successMsg);
    }

    // ==================== Airtime Advance ====================
    public UssdResponse handleAirtimeAdvancePin(UssdRequest request) {
        Map<String, String> session = getSession(request);
        // TODO: check eligibility, then credit airtime
        String successMsg = msg("menu.airtime.advance.success")
                .replace("{phone}", request.msisdn());
        return endSession(successMsg);
    }
}