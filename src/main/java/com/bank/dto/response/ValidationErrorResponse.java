package com.bank.dto.response;

import java.util.Map;

/**
 * Validation error response.
 * Extends the generic ErrorResponse to include field-specific validation
 * errors.
 */
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse(int status, String message, Map<String, String> errors) {
        super(status, message);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
