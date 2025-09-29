package com.bank.exception;

public class UserHasAccountsException extends RuntimeException {
    public UserHasAccountsException(Long id) {
        super(String.format(ErrorMessages.USER_HAS_ACCOUNTS, id));
    }
}