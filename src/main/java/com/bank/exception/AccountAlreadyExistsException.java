package com.bank.exception;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String accountNumber) {
        super(String.format(ErrorMessages.ACCOUNT_ALREADY_EXISTS, accountNumber));
    }
}
