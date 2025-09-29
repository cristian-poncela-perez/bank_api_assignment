package com.bank.dto.response;

/**
 * Simple success response for operations that don't return data.
 * Example: DELETE operations.
 */
public class SuccessResponse {

    private String message;
    private Long timestamp;

    public SuccessResponse() {
    }

    public SuccessResponse(String message, Long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Factory method for creating success responses.
     */
    public static SuccessResponse of(String message) {
        return new SuccessResponse(message, System.currentTimeMillis());
    }
}
