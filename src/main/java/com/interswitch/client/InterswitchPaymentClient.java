package com.interswitch.client;


import com.interswitch.service.OAuth2TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterswitchPaymentClient {

    private final OAuth2TokenService tokenService;
    private final RestClient.Builder restClientBuilder;

    @Value("${interswitch.api.base-url:https://sandbox.interswitchng.com}")
    private String baseUrl;

    // ==================== PURCHASE (CARD) ====================
    public Map<String, Object> initiatePurchase(Map<String, Object> request) {
        String token = tokenService.getAccessToken();
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/v3/purchases")
                .header("Authorization", "Bearer " + token)
                .body(request)
                .retrieve()
                .body(Map.class);
    }

    // OTP validation
    public Map<String, Object> validateOtp(String paymentId, String otp) {
        String token = tokenService.getAccessToken();
        Map<String, Object> body = Map.of("paymentId", paymentId, "otp", otp);
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/v3/purchases/otps/auths")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    // Resend OTP
    public Map<String, Object> resendOtp(String paymentId, String amount, String currency) {
        String token = tokenService.getAccessToken();
        Map<String, Object> body = Map.of("paymentId", paymentId, "amount", amount, "currency", currency);
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/v3/purchases/otps/resend")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    // Transaction status
    public Map<String, Object> getTransactionStatus(String transactionRef, String merchantCode, String amount) {
        String token = tokenService.getAccessToken();
        return restClientBuilder.build()
                .get()
                .uri(baseUrl + "/collections/api/v1/gettransaction.json?transactionReference={ref}&merchantCode={code}&amount={amt}",
                        transactionRef, merchantCode, amount)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);
    }

    // ==================== TRANSFER (INTER-BANK) ====================
    public Map<String, Object> nameEnquiry(String accountNumber, String bankCode) {
        String token = tokenService.getAccessToken();
        Map<String, Object> body = Map.of("accountNumber", accountNumber, "bankCode", bankCode);
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/transfer-service/api/v1/transfers/name-enquiry")
                .header("Authorization", "Bearer " + token)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    public Map<String, Object> singleTransfer(Map<String, Object> transferRequest) {
        String token = tokenService.getAccessToken();
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/transfer-service/api/v1/transfers/single")
                .header("Authorization", "Bearer " + token)
                .body(transferRequest)
                .retrieve()
                .body(Map.class);
    }

    // ==================== BILLS & AIRTIME (NON-CARD) ====================
    public Map<String, Object> payBill(Map<String, Object> billRequest) {
        String token = tokenService.getAccessToken();
        return restClientBuilder.build()
                .post()
                .uri(baseUrl + "/quicktellerservice/api/v5/Transactions")
                .header("Authorization", "Bearer " + token)
                .body(billRequest)
                .retrieve()
                .body(Map.class);
    }

    // Fetch bank list
    public Map<String, Object> getBanks() {
        String token = tokenService.getAccessToken();
        return restClientBuilder.build()
                .get()
                .uri(baseUrl + "/collections/api/v1/ussd/issuers/NG/")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);
    }
}