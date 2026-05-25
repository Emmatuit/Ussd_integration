package com.interswitch.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(SecurityProperties.class)
@TestPropertySource(properties = { "ussd.security.hmac-secret-key=test-hmac-key",
		"ussd.security.aes-secret-key=test-aes-key-16b", "ussd.security.timestamp-threshold-seconds=60" })
class SecurityPropertiesTest {

	@Autowired
	private SecurityProperties properties;

	@Test
	void shouldBindProperties() {
		assertThat(properties.getHmacSecretKey()).isEqualTo("test-hmac-key");
		assertThat(properties.getAesSecretKey()).isEqualTo("test-aes-key-16b");
		assertThat(properties.getTimestampThresholdSeconds()).isEqualTo(60);
	}

	@Test
	void shouldHaveDefaultTimestampThreshold() {
		SecurityProperties defaultProps = new SecurityProperties();
		assertThat(defaultProps.getTimestampThresholdSeconds()).isEqualTo(30);
	}
}