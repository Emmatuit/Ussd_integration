package com.interswitch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "mockbank")
public class MockBankProperties {

    private String defaultPin = "1234";
    private double dailyLimit = 500000;
    private long transactionDelayMs = 500;
    private List<CustomerAccount> accounts = new ArrayList<>();

    @Getter
    @Setter
    public static class CustomerAccount {
        private String msisdn;
        private String accountNumber;
        private String accountName;
        private String accountType;
        private double balance;
        private String currency;
        private String pin;
    }
}