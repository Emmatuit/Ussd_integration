package com.interswitch.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

class AesUtilsTest {

	private static final String SECRET_KEY_16 = "test-key-16bytes";
	private static final String PLAINTEXT = "{\"msisdn\":\"2348012345678\",\"sessionId\":\"abc123\"}";

	@Test
	void shouldDecryptWhatWasEncrypted() throws Exception {
		String iv = generateIv();
		String encrypted = encrypt(PLAINTEXT, SECRET_KEY_16, iv);
		String decrypted = AesUtils.decrypt(encrypted, SECRET_KEY_16, iv);

		assertThat(decrypted).isEqualTo(PLAINTEXT);
	}

	@Test
	void shouldFailWithWrongKey() throws Exception {
		String iv = generateIv();
		String encrypted = encrypt(PLAINTEXT, SECRET_KEY_16, iv);

		assertThatThrownBy(() -> AesUtils.decrypt(encrypted, "wrong-key-16bytes", iv))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("AES decryption failed");
	}

	@Test
	void shouldProduceGarbledOutputWithWrongIv() throws Exception {
		String iv = generateIv();
		String wrongIv = generateIv();
		String encrypted = encrypt(PLAINTEXT, SECRET_KEY_16, iv);

		String decrypted = AesUtils.decrypt(encrypted, SECRET_KEY_16, wrongIv);

		assertThat(decrypted).isNotEqualTo(PLAINTEXT);
	}

	@Test
	void shouldRejectInvalidKeyLength() {
		String shortKey = "short";
		String iv = generateIv();

		assertThatThrownBy(() -> AesUtils.decrypt("test", shortKey, iv)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("AES decryption failed");
	}

	private String generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return Base64.getEncoder().encodeToString(iv);
	}

	private String encrypt(String plaintext, String key, String ivBase64) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
		byte[] iv = Base64.getDecoder().decode(ivBase64);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encrypted);
	}
}