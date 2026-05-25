package com.interswitch.controller;

import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interswitch.dto.request.AirtimeRequest;
import com.interswitch.dto.response.AccountResponse;
import com.interswitch.dto.response.TransactionResponse;
import com.interswitch.service.MockBankService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mock-bank/api/transactions")
@Profile("h2")
@RequiredArgsConstructor
public class TransactionController {

	private final MockBankService mockBankService;

	@PostMapping("/airtime")
	public ResponseEntity<TransactionResponse> purchaseAirtime(@Valid @RequestBody AirtimeRequest request) {
		return ResponseEntity
				.ok(mockBankService.processAirtime(request.getAccountNumber(), request.getAmount(), request.getPin()));
	}

	@PostMapping("/transfer")
	public ResponseEntity<TransactionResponse> transfer(@RequestBody Map<String, String> request) {
		return ResponseEntity.ok(mockBankService.processTransfer(request.get("accountNumber"), request.get("amount"),
				request.get("pin"), request.get("destAccount"), request.get("destBank")));
	}

	@PostMapping("/balance")
	public ResponseEntity<AccountResponse> checkBalance(@RequestBody Map<String, String> request) {
		return ResponseEntity.ok(mockBankService.checkBalance(request.get("accountNumber"), request.get("pin")));
	}

	@PostMapping("/change-pin")
	public ResponseEntity<Map<String, String>> changePin(@RequestBody Map<String, String> request) {
		mockBankService.changePin(request.get("accountNumber"), request.get("oldPin"), request.get("newPin"));
		return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "PIN changed"));
	}
}