package com.interswitch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ussd.security")
public class SecurityProperties {

    private String hmacSecretKey;
    private String aesSecretKey;
    private int timestampThresholdSeconds = 30;
    private boolean hmacEnabled = true; // false = bypass for dev/test
}