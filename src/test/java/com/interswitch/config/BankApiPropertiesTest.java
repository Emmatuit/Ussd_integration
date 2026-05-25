package com.interswitch.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(BankApiProperties.class)
@TestPropertySource(properties = { "bank.api.base-url=http://test-bank:8080", "bank.api.connection-timeout=3000",
		"bank.api.read-timeout=8000" })
class BankApiPropertiesTest {

	@Autowired
	private BankApiProperties properties;

	@Test
	void shouldBindProperties() {
		assertThat(properties.getBaseUrl()).isEqualTo("http://test-bank:8080");
		assertThat(properties.getConnectionTimeout()).isEqualTo(3000);
		assertThat(properties.getReadTimeout()).isEqualTo(8000);
	}

	@Test
	void shouldHaveDefaultValues() {
		BankApiProperties defaultProps = new BankApiProperties();
		assertThat(defaultProps.getBaseUrl()).isEqualTo("http://localhost:9090");
		assertThat(defaultProps.getConnectionTimeout()).isEqualTo(5000);
		assertThat(defaultProps.getReadTimeout()).isEqualTo(10000);
	}
}