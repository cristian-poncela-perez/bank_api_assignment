package com.bank.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

import com.bank.exception.ErrorMessages;

public class UpdateBalanceRequest {

    @NotNull(message = ErrorMessages.BALANCE_REQUIRED)
    @PositiveOrZero(message = ErrorMessages.BALANCE_NON_NEGATIVE)
    private BigDecimal balance;

    public UpdateBalanceRequest() {
    }

    public UpdateBalanceRequest(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}