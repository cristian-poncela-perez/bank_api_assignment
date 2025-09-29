package com.bank.exception;

/**
 * Centralized error messages for exceptions and validation.
 * This allows consistent messaging and easier testing.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Prevent instantiation
    }

    // User-related messages
    public static final String USER_NOT_FOUND = "User not found with ID: %d";
    public static final String USER_ALREADY_EXISTS = "User already exists with email: %s";
    public static final String USER_HAS_ACCOUNTS = "Cannot delete user with ID %d because they have associated accounts";

    // Account-related messages
    public static final String ACCOUNT_NOT_FOUND = "Account not found with ID: %d";
    public static final String ACCOUNT_ALREADY_EXISTS = "Account already exists with account number: %s";
    public static final String ACCOUNT_BALANCE_NOT_ZERO = "Cannot delete account with ID %d because balance is not zero";

    // Account-User relationship messages
    public static final String USER_ALREADY_ASSOCIATED = "User with ID %d is already associated with account ID %d";

    // Validation messages
    public static final String METRICS_PARAMETERS_REQUIRED = "At least one of greaterThan or lessThan parameter must be provided";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email should be valid";
    public static final String ACCOUNT_NUMBER_REQUIRED = "Account number is required";
    public static final String BALANCE_REQUIRED = "Balance is required";
    public static final String BALANCE_NON_NEGATIVE = "Balance must be positive or zero";
    public static final String PRIMARY_USER_ID_REQUIRED = "Primary user ID is required";
    public static final String USER_ID_REQUIRED = "User ID is required";
}