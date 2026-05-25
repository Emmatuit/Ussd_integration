package com.interswitch.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interswitch.client.BankApiClient;
import com.interswitch.dto.bank.AccountDto;
import com.interswitch.dto.bank.TransactionResultDto;
import com.interswitch.dto.request.UssdRequest;
import com.interswitch.util.HmacUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class UssdFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private BankApiClient bankApiClient;

	// Must match ussd.security.hmac-secret-key in application-h2.properties
	private static final String TEST_HMAC_KEY = "default-hmac-key-change-in-production";

	private MockHttpServletRequestBuilder signedPost(String uri, String body) throws Exception {
		long timestamp = System.currentTimeMillis();
		String dataToSign = uri + timestamp + body;
		String signature = HmacUtils.computeHmac(dataToSign, TEST_HMAC_KEY);

		return post(uri).contentType(MediaType.APPLICATION_JSON).content(body).header("SIGNATURE", signature)
				.header("TIMESTAMP", String.valueOf(timestamp));
	}

	@Test
	void shouldCompleteFullAirtimePurchaseFlow() throws Exception {
		// Arrange
		AccountDto account = new AccountDto("1234567890", "John Doe", "Savings", "5000.00", "NGN");
		when(bankApiClient.fetchAccounts(anyString())).thenReturn(List.of(account));

		TransactionResultDto transactionResult = new TransactionResultDto("TXN-001", "SUCCESS", "Airtime purchased",
				"REF-001", "1000");
		when(bankApiClient.purchaseAirtime(anyString(), anyString(), anyString())).thenReturn(transactionResult);

		// Step 1 - initiate airtime purchase
		UssdRequest step1Request = new UssdRequest("2348012345678", "session-integration-001", "1000", new HashMap<>(),
				true);
		String step1Body = objectMapper.writeValueAsString(step1Request);

		String step1Response = mockMvc.perform(signedPost("/api/ussd/airtime", step1Body)).andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Select account")))
				.andExpect(jsonPath("$.callbackUrl").value("/api/ussd/select-account"))
				.andExpect(jsonPath("$.sessionData.amount").value("1000")).andReturn().getResponse()
				.getContentAsString();

		@SuppressWarnings("unchecked")
		Map<String, Object> step1Map = objectMapper.readValue(step1Response, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sessionData1 = (Map<String, String>) step1Map.get("sessionData");

		// Step 2 - select account
		UssdRequest step2Request = new UssdRequest("2348012345678", "session-integration-001", "1", sessionData1,
				false);
		String step2Body = objectMapper.writeValueAsString(step2Request);

		String step2Response = mockMvc.perform(signedPost("/api/ussd/select-account", step2Body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value("Enter your 4-digit PIN\nto confirm purchase"))
				.andExpect(jsonPath("$.callbackUrl").value("/api/ussd/purchase")).andReturn().getResponse()
				.getContentAsString();

		@SuppressWarnings("unchecked")
		Map<String, Object> step2Map = objectMapper.readValue(step2Response, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, String> sessionData2 = (Map<String, String>) step2Map.get("sessionData");

		// Step 3 - confirm purchase with PIN
		UssdRequest step3Request = new UssdRequest("2348012345678", "session-integration-001", "1234", sessionData2,
				false);
		String step3Body = objectMapper.writeValueAsString(step3Request);

		mockMvc.perform(signedPost("/api/ussd/purchase", step3Body)).andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Airtime purchased!")))
				.andExpect(jsonPath("$.sessionEnd").value(true));
	}

	@Test
	void shouldReturnErrorForInvalidStep() throws Exception {
		// Arrange - no accounts returned
		when(bankApiClient.fetchAccounts(anyString())).thenReturn(List.of());

		UssdRequest request = new UssdRequest("2348012345678", "session-no-accounts", "1000", new HashMap<>(), true);
		String body = objectMapper.writeValueAsString(request);

		mockMvc.perform(signedPost("/api/ussd/airtime", body)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.sessionEnd").value(true));
	}
}