package com.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import com.bank.service.AccountService;
import com.bank.dto.request.AddAuthorizedUserRequest;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.UpdateAccountRequest;
import com.bank.dto.request.UpdateBalanceRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.SuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@Validated
@Tag(name = "Accounts", description = "Account management operations")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    @Operation(summary = "Get all accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id) {
        AccountResponse account = accountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update account details")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse updatedAccount = accountService.updateAccount(id, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @PatchMapping("/{id}/balance")
    @Operation(summary = "Update account balance")
    public ResponseEntity<AccountResponse> updateBalance(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBalanceRequest request) {
        AccountResponse updatedAccount = accountService.updateBalance(id, request.getBalance());
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/{id}/authorized-users")
    @Operation(summary = "Add an authorized user to the account")
    public ResponseEntity<AccountResponse> addAuthorizedUser(
            @PathVariable Long id,
            @Valid @RequestBody AddAuthorizedUserRequest request) {
        return ResponseEntity.ok(accountService.addAuthorizedUser(id, request.getUserId()));
    }

    @DeleteMapping("/{id}/authorized-users/{userId}")
    @Operation(summary = "Remove an authorized user from the account")
    public ResponseEntity<AccountResponse> removeAuthorizedUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(accountService.removeAuthorizedUser(id, userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account")
    public ResponseEntity<SuccessResponse> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.ok(SuccessResponse.of("Account deleted successfully"));
    }
}