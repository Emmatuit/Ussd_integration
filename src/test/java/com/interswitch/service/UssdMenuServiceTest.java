package com.interswitch.service;

import com.interswitch.constant.SessionKeys;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import com.interswitch.service.menu.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.HashMap;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UssdMenuServiceTest {

    @Mock private MessageSource messageSource;
    @Mock private AuditLogService auditLogService;
    @Mock private TransferMenuHandler transferHandler;
    @Mock private AirtimeDataMenuHandler airtimeDataHandler;
    @Mock private BillsMenuHandler billsHandler;
    @Mock private AccountServicesMenuHandler accountHandler;
    @Mock private SecurityMenuHandler securityHandler;

    @InjectMocks
    private UssdMenuService menuService;

    private UssdRequest request;

    @BeforeEach
    void setUp() {
        request = new UssdRequest("2348012345678", "session-123", "", new HashMap<>(), true);
        when(messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0)); // return key as dummy text
    }

    @Test
    void handleMainMenu_ShouldReturnMainMenuText() {
        UssdResponse response = menuService.handleMainMenu(request);
        assertThat(response.text()).isEqualTo("menu.main");
        assertThat(response.callbackUrl()).isEqualTo("/api/ussd/menu-choice");
        assertThat(response.sessionData()).containsKey(SessionKeys.FLOW);
    }

    @Test
    void handleMainMenuChoice_Option1_ShouldCallTransferHandler() {
        request = new UssdRequest("2348012345678", "session-123", "1", new HashMap<>(), true);
        when(transferHandler.showTransferMenu(any())).thenReturn(
                UssdResponse.builder().text("transfer menu").callbackUrl("/transfer").build()
        );
        UssdResponse response = menuService.handleMainMenuChoice(request);
        assertThat(response.text()).isEqualTo("transfer menu");
    }

    @Test
    void handleMainMenuChoice_Option2_ShouldCallAirtimeHandler() {
        request = new UssdRequest("2348012345678", "session-123", "2", new HashMap<>(), true);
        when(airtimeDataHandler.showMenu(any())).thenReturn(
                UssdResponse.builder().text("airtime menu").callbackUrl("/airdata").build()
        );
        UssdResponse response = menuService.handleMainMenuChoice(request);
        assertThat(response.text()).isEqualTo("airtime menu");
    }

    @Test
    void handleMainMenuChoice_Option0_ShouldExit() {
        request = new UssdRequest("2348012345678", "session-123", "0", new HashMap<>(), true);
        UssdResponse response = menuService.handleMainMenuChoice(request);
        assertThat(response.sessionEnd()).isTrue();
        assertThat(response.text()).isEqualTo("menu.exit");
    }
}