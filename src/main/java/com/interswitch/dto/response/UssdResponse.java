package com.interswitch.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UssdResponse(String text,

		@JsonProperty("callbackUrl") String callbackUrl,

		@JsonProperty("sessionData") Map<String, String> sessionData,

		@JsonProperty("sessionEnd") Boolean sessionEnd) {
}