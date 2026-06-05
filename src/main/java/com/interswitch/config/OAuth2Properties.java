package com.interswitch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "interswitch.oauth2")
public class OAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String tokenUrl = "https://sandbox.interswitchng.com/passport/oauth/token";
}