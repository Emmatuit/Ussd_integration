package com.interswitch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final BankApiProperties bankApiProperties;

    @Bean
    public RestClient bankRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(bankApiProperties.getConnectionTimeout());
        factory.setReadTimeout(bankApiProperties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(bankApiProperties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    // Add this bean to provide RestClient.Builder for InterswitchPaymentClient
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}