package com.interswitch.controller;

import com.interswitch.dto.request.UssdRequest;
import com.interswitch.dto.response.UssdResponse;
import com.interswitch.service.UssdMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
@Slf4j
public class UssdController {

    private final UssdMenuService menuService;

    // Main menu
    @PostMapping("/menu")
    public ResponseEntity<UssdResponse> showMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleMainMenu(request));
    }

    @PostMapping("/menu-choice")
    public ResponseEntity<UssdResponse> menuChoice(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleMainMenuChoice(request));
    }

    // Transfer
    @PostMapping("/transfer/sub-menu")
    public ResponseEntity<UssdResponse> transferSubMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferSubMenuChoice(request));
    }

    @PostMapping("/transfer/others-menu")
    public ResponseEntity<UssdResponse> transferOthersMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOthersMenuChoice(request));
    }

    @PostMapping("/transfer/self/amount")
    public ResponseEntity<UssdResponse> transferSelfAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferSelfAmount(request));
    }

    @PostMapping("/transfer/self/pin")
    public ResponseEntity<UssdResponse> transferSelfPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferSelfPin(request));
    }

    @PostMapping("/transfer/other-olive/amount")
    public ResponseEntity<UssdResponse> transferOtherOliveAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherOliveAmount(request));
    }

    @PostMapping("/transfer/other-olive/account")
    public ResponseEntity<UssdResponse> transferOtherOliveAccount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherOliveAccount(request));
    }

    @PostMapping("/transfer/other-olive/pin")
    public ResponseEntity<UssdResponse> transferOtherOlivePin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherOlivePin(request));
    }

    @PostMapping("/transfer/other-bank/amount")
    public ResponseEntity<UssdResponse> transferOtherBankAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherBankAmount(request));
    }

    @PostMapping("/transfer/other-bank/account")
    public ResponseEntity<UssdResponse> transferOtherBankAccount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherBankAccount(request));
    }

    @PostMapping("/transfer/other-bank/bank")
    public ResponseEntity<UssdResponse> transferOtherBankBank(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherBankSelection(request));
    }

    @PostMapping("/transfer/other-bank/confirm")
    public ResponseEntity<UssdResponse> transferOtherBankConfirm(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherBankConfirm(request));
    }

    @PostMapping("/transfer/other-ewallet/amount")
    public ResponseEntity<UssdResponse> transferOtherEwalletAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherEwalletAmount(request));
    }

    @PostMapping("/transfer/other-ewallet/account")
    public ResponseEntity<UssdResponse> transferOtherEwalletAccount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherEwalletAccount(request));
    }

    @PostMapping("/transfer/other-ewallet/provider")
    public ResponseEntity<UssdResponse> transferOtherEwalletProvider(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherEwalletProvider(request));
    }

    @PostMapping("/transfer/other-ewallet/pin")
    public ResponseEntity<UssdResponse> transferOtherEwalletPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTransferOtherEwalletPin(request));
    }

    // Airtime & Data
    @PostMapping("/airdata-menu")
    public ResponseEntity<UssdResponse> airdataMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeDataMenuChoice(request));
    }

    @PostMapping("/airtime/self/amount")
    public ResponseEntity<UssdResponse> airtimeSelfAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeSelfAmount(request));
    }

    @PostMapping("/airtime/others/phone")
    public ResponseEntity<UssdResponse> airtimeOthersPhone(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeOthersPhone(request));
    }

    @PostMapping("/airtime/others/amount")
    public ResponseEntity<UssdResponse> airtimeOthersAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeOthersAmount(request));
    }

    @PostMapping("/airtime/others/pin")
    public ResponseEntity<UssdResponse> airtimeOthersPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeOthersPin(request));
    }

    @PostMapping("/data/menu")
    public ResponseEntity<UssdResponse> dataMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataMenuChoice(request));
    }

    @PostMapping("/data/self/plan")
    public ResponseEntity<UssdResponse> dataSelfPlan(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataSelfPlan(request));
    }

    @PostMapping("/data/self/pin")
    public ResponseEntity<UssdResponse> dataSelfPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataSelfPin(request));
    }

    @PostMapping("/data/others/phone")
    public ResponseEntity<UssdResponse> dataOthersPhone(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataOthersPhone(request));
    }

    @PostMapping("/data/others/network")
    public ResponseEntity<UssdResponse> dataOthersNetwork(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataOthersNetwork(request));
    }

    @PostMapping("/data/others/plan")
    public ResponseEntity<UssdResponse> dataOthersPlan(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataOthersPlan(request));
    }

    @PostMapping("/data/others/pin")
    public ResponseEntity<UssdResponse> dataOthersPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleDataOthersPin(request));
    }

    @PostMapping("/airtime/advance/pin")
    public ResponseEntity<UssdResponse> airtimeAdvancePin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAirtimeAdvancePin(request));
    }

    // Bills & Utilities
    @PostMapping("/bills-menu")
    public ResponseEntity<UssdResponse> billsMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsMenuChoice(request));
    }

    @PostMapping("/cable/provider")
    public ResponseEntity<UssdResponse> cableProvider(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleCableProvider(request));
    }

    @PostMapping("/cable/smartcard")
    public ResponseEntity<UssdResponse> cableSmartcard(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleCableSmartcard(request));
    }

    @PostMapping("/cable/bouquet")
    public ResponseEntity<UssdResponse> cableBouquet(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleCableBouquet(request));
    }

    @PostMapping("/cable/pin")
    public ResponseEntity<UssdResponse> cablePin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleCablePin(request));
    }

    @PostMapping("/bills/category")
    public ResponseEntity<UssdResponse> billsCategory(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsCategory(request));
    }

    @PostMapping("/bills/provider")
    public ResponseEntity<UssdResponse> billsProvider(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsProvider(request));
    }

    @PostMapping("/bills/reference")
    public ResponseEntity<UssdResponse> billsReference(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsReference(request));
    }

    @PostMapping("/bills/amount")
    public ResponseEntity<UssdResponse> billsAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsAmount(request));
    }

    @PostMapping("/bills/pin")
    public ResponseEntity<UssdResponse> billsPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleBillsPin(request));
    }

    @PostMapping("/balance/execute")
    public ResponseEntity<UssdResponse> balanceExecute(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.executeBalanceCheck(request));
    }

    // Account Services
    @PostMapping("/account-menu")
    public ResponseEntity<UssdResponse> accountMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleAccountMenuChoice(request));
    }

    @PostMapping("/openaccount/name")
    public ResponseEntity<UssdResponse> openAccountName(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOpenAccountName(request));
    }

    @PostMapping("/openaccount/dob")
    public ResponseEntity<UssdResponse> openAccountDob(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOpenAccountDob(request));
    }

    @PostMapping("/openaccount/phone")
    public ResponseEntity<UssdResponse> openAccountPhone(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOpenAccountPhone(request));
    }

    @PostMapping("/openaccount/type")
    public ResponseEntity<UssdResponse> openAccountType(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOpenAccountType(request));
    }

    @PostMapping("/openaccount/confirm")
    public ResponseEntity<UssdResponse> openAccountConfirm(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOpenAccountConfirm(request));
    }

    @PostMapping("/loan/balance/pin")
    public ResponseEntity<UssdResponse> loanBalancePin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleLoanBalancePin(request));
    }

    @PostMapping("/token/amount")
    public ResponseEntity<UssdResponse> tokenAmount(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTokenAmount(request));
    }

    @PostMapping("/token/pin")
    public ResponseEntity<UssdResponse> tokenPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleTokenPin(request));
    }

    // Security & Settings
    @PostMapping("/security-menu")
    public ResponseEntity<UssdResponse> securityMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleSecurityMenuChoice(request));
    }

    @PostMapping("/pin/menu")
    public ResponseEntity<UssdResponse> pinMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handlePinMenuChoice(request));
    }

    @PostMapping("/pin/change/old")
    public ResponseEntity<UssdResponse> pinChangeOld(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleChangePinOld(request));
    }

    @PostMapping("/pin/change/new")
    public ResponseEntity<UssdResponse> pinChangeNew(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleChangePinNew(request));
    }

    @PostMapping("/pin/change/confirm")
    public ResponseEntity<UssdResponse> pinChangeConfirm(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleChangePinConfirm(request));
    }

    @PostMapping("/pin/reset/phone")
    public ResponseEntity<UssdResponse> pinResetPhone(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleResetPinPhone(request));
    }

    @PostMapping("/pin/reset/otp")
    public ResponseEntity<UssdResponse> pinResetOtp(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleResetPinOtp(request));
    }

    @PostMapping("/pin/reset/new")
    public ResponseEntity<UssdResponse> pinResetNew(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleResetPinNew(request));
    }

    @PostMapping("/pin/reset/confirm")
    public ResponseEntity<UssdResponse> pinResetConfirm(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleResetPinConfirm(request));
    }

    @PostMapping("/opt/menu")
    public ResponseEntity<UssdResponse> optMenu(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOptMenuChoice(request));
    }

    @PostMapping("/optin/pin")
    public ResponseEntity<UssdResponse> optInPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOptInPin(request));
    }

    @PostMapping("/optin/card")
    public ResponseEntity<UssdResponse> optInCard(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOptInCard(request));
    }

    @PostMapping("/optout/pin")
    public ResponseEntity<UssdResponse> optOutPin(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOptOutPin(request));
    }

    @PostMapping("/optout/card")
    public ResponseEntity<UssdResponse> optOutCard(@Valid @RequestBody UssdRequest request) {
        return ResponseEntity.ok(menuService.handleOptOutCard(request));
    }
}