package com.interswitch.filter;

import com.interswitch.config.SecurityProperties;
import com.interswitch.constant.ErrorCode;
import com.interswitch.exception.CustomSecurityException;
import com.interswitch.util.AesUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class AesDecryptionFilter extends OncePerRequestFilter {

    private static final String IV_HEADER = "IV";
    private static final String ENCRYPTED_CONTENT_TYPE = "text/plain";

    private final SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (securityProperties.isHmacEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.startsWith("/api/ussd/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (securityProperties.isHmacEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String contentType = request.getContentType();

        if (contentType == null || !contentType.equalsIgnoreCase(ENCRYPTED_CONTENT_TYPE)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ivHeader = request.getHeader(IV_HEADER);
        if (ivHeader == null || ivHeader.isEmpty()) {
            log.warn("IV header missing for encrypted request");
            throw new CustomSecurityException(ErrorCode.DECRYPTION_FAILED);
        }

        CachedBodyRequestWrapper cachedRequest;
        if (request instanceof CachedBodyRequestWrapper) {
            cachedRequest = (CachedBodyRequestWrapper) request;
        } else {
            cachedRequest = new CachedBodyRequestWrapper(request);
        }

        String encryptedBody = cachedRequest.getBody();

        try {
            String decryptedBody = AesUtils.decrypt(
                    encryptedBody,
                    securityProperties.getAesSecretKey(),
                    ivHeader
            );

            DecryptedBodyRequestWrapper decryptedRequest = new DecryptedBodyRequestWrapper(request, decryptedBody);
            filterChain.doFilter(decryptedRequest, response);
        } catch (Exception e) {
            log.error("AES decryption failed", e);
            throw new CustomSecurityException(ErrorCode.DECRYPTION_FAILED);
        }
    }
}