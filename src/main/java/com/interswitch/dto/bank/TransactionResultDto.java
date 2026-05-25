package com.interswitch.dto.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record TransactionResultDto(@NotBlank @JsonProperty("transactionId") String transactionId,

		@NotBlank @JsonProperty("status") String status,

		@JsonProperty("message") String message,

		@JsonProperty("referenceNumber") String referenceNumber,

		@JsonProperty("amount") String amount) {
}