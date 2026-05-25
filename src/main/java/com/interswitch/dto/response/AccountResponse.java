package com.interswitch.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

	@JsonProperty("accountNumber")
	private String accountNumber;

	@JsonProperty("accountName")
	private String accountName;

	@JsonProperty("accountType")
	private String accountType;

	@JsonProperty("balance")
	private String balance;

	@JsonProperty("currency")
	private String currency;
}