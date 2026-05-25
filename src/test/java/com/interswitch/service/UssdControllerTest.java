package com.interswitch.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interswitch.controller.UssdController;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;

@WebMvcTest(UssdController.class)
class UssdControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UssdFlowService ussdFlowService;

	@Test
	void shouldReturnAirtimeInitResponse() throws Exception {
		UssdResponse mockResponse = UssdResponse.builder().text("Select account:\n1. 123****890 (Savings)")
				.callbackUrl("/api/ussd/select-account").sessionData(new HashMap<>()).build();

		when(ussdFlowService.handleAirtimeInit(any(UssdRequest.class))).thenReturn(mockResponse);

		UssdRequest request = new UssdRequest("2348012345678", "session-123", "1000", new HashMap<>(), true);

		mockMvc.perform(post("/api/ussd/airtime").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value("Select account:\n1. 123****890 (Savings)"))
				.andExpect(jsonPath("$.callbackUrl").value("/api/ussd/select-account"));
	}

	@Test
	void shouldReturnBadRequestForInvalidPayload() throws Exception {
		mockMvc.perform(post("/api/ussd/airtime").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnPurchaseResponse() throws Exception {
		UssdResponse mockResponse = UssdResponse.builder().text("Airtime purchased!\nAmt: 1000\nRef: REF456")
				.sessionEnd(true).sessionData(new HashMap<>()).build();

		when(ussdFlowService.handlePurchase(any(UssdRequest.class))).thenReturn(mockResponse);

		UssdRequest request = new UssdRequest("2348012345678", "session-123", "1234", new HashMap<>(), false);

		mockMvc.perform(post("/api/ussd/purchase").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.sessionEnd").value(true));
	}

	@Test
	void shouldReturnSelectAccountResponse() throws Exception {
		UssdResponse mockResponse = UssdResponse.builder().text("Enter your 4-digit PIN\nto confirm purchase")
				.callbackUrl("/api/ussd/purchase").sessionData(new HashMap<>()).build();

		when(ussdFlowService.handleSelectAccount(any(UssdRequest.class))).thenReturn(mockResponse);

		UssdRequest request = new UssdRequest("2348012345678", "session-123", "1", new HashMap<>(), false);

		mockMvc.perform(post("/api/ussd/select-account").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.callbackUrl").value("/api/ussd/purchase"));
	}
}
