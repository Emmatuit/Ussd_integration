package com.interswitch.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UssdRequest(@NotBlank(message = "msisdn is required") String msisdn,

		@NotBlank(message = "sessionId is required") String sessionId,

		String input,

		Map<String, String> sessionData,

		@JsonProperty("sessionStart") @NotNull(message = "sessionStart is required") Boolean sessionStart) {
}