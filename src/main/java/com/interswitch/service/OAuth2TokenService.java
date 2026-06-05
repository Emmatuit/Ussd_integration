//package com.interswitch.service;
//
//import com.interswitch.config.OAuth2Properties;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.time.Instant;
//import java.util.Base64;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OAuth2TokenService {
//
//    private final OAuth2Properties oauth2Props;
//    private String cachedToken;
//    private Instant tokenExpiry;
//
//    public String getAccessToken() {
//        if (cachedToken != null && tokenExpiry != null && tokenExpiry.isAfter(Instant.now())) {
//            return cachedToken;
//        }
//
//        String credentials = oauth2Props.getClientId() + ":" + oauth2Props.getClientSecret();
//        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
//
//        var response = RestClient.create()
//                .post()
//                .uri(oauth2Props.getTokenUrl())
//                .header("Authorization", "Basic " + encoded)
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .body("grant_type=client_credentials")
//                .retrieve()
//                .body(TokenResponse.class);
//
//        if (response == null || response.getAccessToken() == null) {
//            throw new RuntimeException("Failed to obtain access token");
//        }
//
//        this.cachedToken = response.getAccessToken();
//        this.tokenExpiry = Instant.now().plusSeconds(response.getExpiresIn() - 60);
//        log.info("OAuth2 token obtained, expires at {}", tokenExpiry);
//        return cachedToken;
//    }
//
//    private record TokenResponse(String access_token, String token_type, long expires_in) {
//        public String getAccessToken() { return access_token; }
//        public long getExpiresIn() { return expires_in; }
//    }
//}

package com.interswitch.service;

import com.interswitch.config.OAuth2Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2TokenService {

    private final OAuth2Properties oauth2Props;

    @Value("${interswitch.oauth2.test-mode:true}")
    private boolean testMode;

    private String cachedToken;
    private Instant tokenExpiry;

    public String getAccessToken() {
        if (testMode) {
            log.debug("Test mode enabled – returning fake token");
            return "fake-test-token-for-sandbox";
        }

        if (cachedToken != null && tokenExpiry != null && tokenExpiry.isAfter(Instant.now())) {
            return cachedToken;
        }

        String credentials = oauth2Props.getClientId() + ":" + oauth2Props.getClientSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        var response = RestClient.create()
                .post()
                .uri(oauth2Props.getTokenUrl())
                .header("Authorization", "Basic " + encoded)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body("grant_type=client_credentials")
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Failed to obtain access token");
        }

        this.cachedToken = response.getAccessToken();
        this.tokenExpiry = Instant.now().plusSeconds(response.getExpiresIn() - 60);
        log.info("OAuth2 token obtained, expires at {}", tokenExpiry);
        return cachedToken;
    }

    private record TokenResponse(String access_token, String token_type, long expires_in) {
        public String getAccessToken() { return access_token; }
        public long getExpiresIn() { return expires_in; }
    }
}