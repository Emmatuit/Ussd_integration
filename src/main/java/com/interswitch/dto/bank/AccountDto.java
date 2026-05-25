package com.interswitch.dto.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record AccountDto(@NotBlank @JsonProperty("accountNumber") String accountNumber,

		@JsonProperty("accountName") String accountName,

		@JsonProperty("accountType") String accountType,

		@JsonProperty("balance") String balance,

		@JsonProperty("currency") String currency) {
}