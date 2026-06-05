package com.interswitch.service.menu;

import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public abstract class BaseMenuHandler {

    protected final MessageSource messageSource;

    protected Map<String, String> getSession(UssdRequest request) {
        return request.sessionData() != null ? new HashMap<>(request.sessionData()) : new HashMap<>();
    }

    protected UssdResponse response(String text, String callbackUrl, Map<String, String> session) {
        return UssdResponse.builder()
                .text(text)
                .callbackUrl(callbackUrl)
                .sessionData(session)
                .build();
    }

    protected UssdResponse endSession(String message) {
        return UssdResponse.builder()
                .text(message)
                .sessionEnd(true)
                .sessionData(new HashMap<>())
                .build();
    }

    protected UssdResponse invalidOption(UssdRequest request, String callbackUrl) {
        Map<String, String> session = getSession(request);
        return response(msg("menu.invalid.option"), callbackUrl, session);
    }

    protected String msg(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }
}