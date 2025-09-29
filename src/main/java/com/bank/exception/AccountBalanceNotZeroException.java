package com.bank.exception;

public class AccountBalanceNotZeroException extends RuntimeException {
    public AccountBalanceNotZeroException(Long id) {
        super(String.format(ErrorMessages.ACCOUNT_BALANCE_NOT_ZERO, id));
    }
}