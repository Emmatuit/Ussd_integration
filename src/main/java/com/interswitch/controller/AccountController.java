package com.interswitch.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interswitch.dto.response.AccountResponse;
import com.interswitch.service.MockBankService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mock-bank/api")
@Profile("h2")
@RequiredArgsConstructor
public class AccountController {

	private final MockBankService mockBankService;

	@GetMapping("/accounts/{msisdn}")
	public ResponseEntity<List<AccountResponse>> fetchAccounts(@PathVariable String msisdn) {
		return ResponseEntity.ok(mockBankService.fetchAccounts(msisdn));
	}
}