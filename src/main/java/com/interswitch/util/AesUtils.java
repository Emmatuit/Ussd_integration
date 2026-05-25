package com.interswitch.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AesUtils {

	private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

	public static String decrypt(String encryptedBody, String secretKey, String ivBase64) {
		try {
			byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
			if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
				throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes");
			}

			SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
			byte[] iv = Base64.getDecoder().decode(ivBase64);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBody);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("Failed to decrypt request body", e);
			throw new RuntimeException("AES decryption failed", e);
		}
	}

	private AesUtils() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}