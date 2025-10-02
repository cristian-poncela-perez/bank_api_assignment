package com.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super(String.format(ErrorMessages.ACCOUNT_NOT_FOUND, id));
    }
}