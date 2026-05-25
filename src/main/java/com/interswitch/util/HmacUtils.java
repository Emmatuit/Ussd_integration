package com.interswitch.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HmacUtils {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	public static String computeHmac(String data, String secretKey) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
			mac.init(keySpec);
			byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(hmacBytes);
		} catch (Exception e) {
			log.error("Failed to compute HMAC", e);
			throw new RuntimeException("HMAC computation failed", e);
		}
	}

	public static boolean verifyHmac(String data, String secretKey, String expectedSignature) {
		String computedSignature = computeHmac(data, secretKey);
		return computedSignature.equals(expectedSignature);
	}

	private HmacUtils() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}
