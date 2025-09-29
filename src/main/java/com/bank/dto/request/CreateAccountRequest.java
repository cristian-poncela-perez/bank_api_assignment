package com.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

import com.bank.exception.ErrorMessages;

public class CreateAccountRequest {

    @NotBlank(message = ErrorMessages.ACCOUNT_NUMBER_REQUIRED)
    private String accountNumber;

    @NotNull(message = ErrorMessages.BALANCE_REQUIRED)
    @PositiveOrZero(message = ErrorMessages.BALANCE_NON_NEGATIVE)
    private BigDecimal balance;

    @NotNull(message = ErrorMessages.PRIMARY_USER_ID_REQUIRED)
    private Long primaryUserId;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(String accountNumber, BigDecimal balance, Long primaryUserId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.primaryUserId = primaryUserId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Long getPrimaryUserId() {
        return primaryUserId;
    }

    public void setPrimaryUserId(Long primaryUserId) {
        this.primaryUserId = primaryUserId;
    }
}
