package com.interswitch.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.interswitch.config.SecurityProperties;
import com.interswitch.exception.CustomSecurityException;
import com.interswitch.util.HmacUtils;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class HmacVerificationFilterTest {

    private static final String SECRET_KEY = "test-hmac-secret-key-for-testing";
    private static final String REQUEST_BODY = "{\"msisdn\":\"2348012345678\",\"sessionId\":\"abc\",\"sessionStart\":true}";
    private static final String REQUEST_URI = "/api/ussd/airtime";

    @Mock
    private FilterChain filterChain;

    private SecurityProperties securityProperties;
    private HmacVerificationFilter filter;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.setHmacSecretKey(SECRET_KEY);
        securityProperties.setTimestampThresholdSeconds(30);
        securityProperties.setHmacEnabled(true); // always enabled in tests
        filter = new HmacVerificationFilter(securityProperties);
    }

    @Test
    void shouldNotFilterNonUssdPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/health");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldPassValidRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(REQUEST_BODY.getBytes());

        long timestamp = System.currentTimeMillis();
        String dataToSign = REQUEST_URI + timestamp + REQUEST_BODY;
        String signature = HmacUtils.computeHmac(dataToSign, SECRET_KEY);

        request.addHeader("SIGNATURE", signature);
        request.addHeader("TIMESTAMP", String.valueOf(timestamp));

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletRequest.class),
                org.mockito.ArgumentMatchers.eq(response));
    }

    @Test
    void shouldRejectExpiredTimestamp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(REQUEST_BODY.getBytes());

        long expiredTimestamp = System.currentTimeMillis() - 60_000;
        String dataToSign = REQUEST_URI + expiredTimestamp + REQUEST_BODY;
        String signature = HmacUtils.computeHmac(dataToSign, SECRET_KEY);

        request.addHeader("SIGNATURE", signature);
        request.addHeader("TIMESTAMP", String.valueOf(expiredTimestamp));

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(CustomSecurityException.class);
    }

    @Test
    void shouldRejectMissingSignatureHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(REQUEST_BODY.getBytes());

        long timestamp = System.currentTimeMillis();
        request.addHeader("TIMESTAMP", String.valueOf(timestamp));

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(CustomSecurityException.class);
    }
}