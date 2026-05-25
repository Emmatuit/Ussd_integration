package com.interswitch.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HmacUtilsTest {

	private static final String SECRET_KEY = "test-hmac-secret-key";
	private static final String DATA = "/api/ussd/airtime1712345678000{\"msisdn\":\"2348012345678\"}";

	@Test
	void shouldBeBase64Encoded() {
		String hmac = HmacUtils.computeHmac(DATA, SECRET_KEY);
		assertThat(hmac).matches("^[A-Za-z0-9+/]+=*$");
	}

	@Test
	void shouldComputeConsistentHmac() {
		String hmac1 = HmacUtils.computeHmac(DATA, SECRET_KEY);
		String hmac2 = HmacUtils.computeHmac(DATA, SECRET_KEY);

		assertThat(hmac1).isEqualTo(hmac2);
		assertThat(hmac1).isNotEmpty();
	}

	@Test
	void shouldProduceDifferentHmacForDifferentData() {
		String hmac1 = HmacUtils.computeHmac(DATA, SECRET_KEY);
		String hmac2 = HmacUtils.computeHmac(DATA + "x", SECRET_KEY);

		assertThat(hmac1).isNotEqualTo(hmac2);
	}

	@Test
	void shouldProduceDifferentHmacForDifferentKeys() {
		String hmac1 = HmacUtils.computeHmac(DATA, SECRET_KEY);
		String hmac2 = HmacUtils.computeHmac(DATA, "different-key");

		assertThat(hmac1).isNotEqualTo(hmac2);
	}

	@Test
	void shouldRejectInvalidHmac() {
		boolean result = HmacUtils.verifyHmac(DATA, SECRET_KEY, "invalid-signature");

		assertThat(result).isFalse();
	}

	@Test
	void shouldVerifyValidHmac() {
		String hmac = HmacUtils.computeHmac(DATA, SECRET_KEY);
		boolean result = HmacUtils.verifyHmac(DATA, SECRET_KEY, hmac);

		assertThat(result).isTrue();
	}
}