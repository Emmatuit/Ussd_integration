package com.interswitch.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.interswitch.config.SecurityProperties;
import com.interswitch.constant.ErrorCode;
import com.interswitch.exception.CustomSecurityException;
import com.interswitch.util.HmacUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class HmacVerificationFilter extends OncePerRequestFilter {

    private static final String SIGNATURE_HEADER = "SIGNATURE";
    private static final String TIMESTAMP_HEADER = "TIMESTAMP";

    private final SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!securityProperties.isHmacEnabled()) {
            return true; // bypass in dev/test
        }
        String path = request.getRequestURI();
        return !path.startsWith("/api/ussd/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String signature = request.getHeader(SIGNATURE_HEADER);
        String timestamp = request.getHeader(TIMESTAMP_HEADER);

        if (signature == null || timestamp == null) {
            log.warn("Missing security headers");
            throw new CustomSecurityException(ErrorCode.INVALID_SIGNATURE);
        }

        validateTimestamp(timestamp);

        CachedBodyRequestWrapper cachedRequest = new CachedBodyRequestWrapper(request);
        String rawBody = cachedRequest.getBody();

        String dataToVerify = request.getRequestURI() + timestamp + rawBody;
        boolean isValid = HmacUtils.verifyHmac(dataToVerify, securityProperties.getHmacSecretKey(), signature);

        if (!isValid) {
            log.warn("HMAC signature verification failed for URI: {}", request.getRequestURI());
            throw new CustomSecurityException(ErrorCode.INVALID_SIGNATURE);
        }

        log.debug("HMAC verification successful for URI: {}", request.getRequestURI());
        filterChain.doFilter(cachedRequest, response);
    }

    private void validateTimestamp(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long thresholdMillis = securityProperties.getTimestampThresholdSeconds() * 1000L;

            if (Math.abs(currentTime - requestTime) > thresholdMillis) {
                log.warn("Request timestamp expired. Request: {}, Current: {}", requestTime, currentTime);
                throw new CustomSecurityException(ErrorCode.SIGNATURE_EXPIRED);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid timestamp format: {}", timestamp);
            throw new CustomSecurityException(ErrorCode.INVALID_SIGNATURE);
        }
    }
}