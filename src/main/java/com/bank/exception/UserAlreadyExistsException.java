package com.bank.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super(String.format(ErrorMessages.USER_ALREADY_EXISTS, email));
    }
}