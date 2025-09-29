package com.bank.exception;

public class UserAlreadyAssociatedException extends RuntimeException {
    public UserAlreadyAssociatedException(Long accountId, Long userId) {
        super(String.format(ErrorMessages.USER_ALREADY_ASSOCIATED, userId, accountId));
    }
}
