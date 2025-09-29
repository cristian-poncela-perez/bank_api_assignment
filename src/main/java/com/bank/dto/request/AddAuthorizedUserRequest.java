package com.bank.dto.request;

import com.bank.exception.ErrorMessages;

import jakarta.validation.constraints.NotNull;

public class AddAuthorizedUserRequest {

    @NotNull(message = ErrorMessages.USER_ID_REQUIRED)
    private Long userId;

    public AddAuthorizedUserRequest() {
    }

    public AddAuthorizedUserRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
