package com.bank.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super(String.format(ErrorMessages.USER_NOT_FOUND, id));
    }
}
