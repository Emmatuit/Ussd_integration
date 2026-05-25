package com.interswitch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank.api")
public class BankApiProperties {

	private String baseUrl = "http://localhost:9090";
	private int connectionTimeout = 5000;
	private int readTimeout = 10000;
}