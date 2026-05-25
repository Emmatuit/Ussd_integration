package com.interswitch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import com.interswitch.service.TransferService;
import com.interswitch.service.UssdFlowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
@Slf4j
public class UssdController {

	private final UssdFlowService ussdFlowService;
	private final TransferService transferService;

	// ==================== EXISTING AIRTIME ENDPOINTS ====================

	@PostMapping("/airtime")
	public ResponseEntity<UssdResponse> airtimeInit(@Valid @RequestBody UssdRequest request) {
		log.debug("Received airtime init request: sessionId={}", request.sessionId());
		UssdResponse response = ussdFlowService.handleAirtimeInit(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/select-account")
	public ResponseEntity<UssdResponse> selectAccount(@Valid @RequestBody UssdRequest request) {
		log.debug("Received select account request: sessionId={}", request.sessionId());
		UssdResponse response = ussdFlowService.handleSelectAccount(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/purchase")
	public ResponseEntity<UssdResponse> purchase(@Valid @RequestBody UssdRequest request) {
		log.debug("Received purchase request: sessionId={}", request.sessionId());
		UssdResponse response = ussdFlowService.handlePurchase(request);
		return ResponseEntity.ok(response);
	}

	// ==================== NEW: MENU & TRANSFER ENDPOINTS ====================

	@PostMapping("/menu")
	public ResponseEntity<UssdResponse> showMenu(@Valid @RequestBody UssdRequest request) {
		log.debug("Received menu request: sessionId={}", request.sessionId());
		UssdResponse response = transferService.showMainMenu(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/menu-choice")
	public ResponseEntity<UssdResponse> menuChoice(@Valid @RequestBody UssdRequest request) {
		log.debug("Received menu choice: sessionId={}, input={}", request.sessionId(), request.input());
		UssdResponse response = transferService.handleMenuChoice(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/transfer/amount")
	public ResponseEntity<UssdResponse> transferAmount(@Valid @RequestBody UssdRequest request) {
		log.debug("Received transfer amount: sessionId={}, input={}", request.sessionId(), request.input());
		UssdResponse response = transferService.handleTransferAmount(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/transfer/bank-select")
	public ResponseEntity<UssdResponse> transferBankSelect(@Valid @RequestBody UssdRequest request) {
		log.debug("Received bank select: sessionId={}, input={}", request.sessionId(), request.input());
		UssdResponse response = transferService.showBankSelection(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/transfer/name-enquiry")
	public ResponseEntity<UssdResponse> transferNameEnquiry(@Valid @RequestBody UssdRequest request) {
		log.debug("Received name enquiry: sessionId={}, input={}", request.sessionId(), request.input());
		UssdResponse response = transferService.handleNameEnquiry(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/transfer/confirm")
	public ResponseEntity<UssdResponse> transferConfirm(@Valid @RequestBody UssdRequest request) {
		log.debug("Received transfer confirm: sessionId={}", request.sessionId());
		UssdResponse response = transferService.confirmTransfer(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/balance-check")
	public ResponseEntity<UssdResponse> balanceCheck(@Valid @RequestBody UssdRequest request) {
		log.debug("Received balance check: sessionId={}", request.sessionId());
		UssdResponse response = transferService.handleBalanceCheck(request);
		return ResponseEntity.ok(response);
	}
}