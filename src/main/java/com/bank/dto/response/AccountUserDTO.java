package com.bank.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Unified DTO representing the relationship between a User and an Account.
 * 
 * <p>
 * This DTO is used in two contexts:
 * </p>
 * <ul>
 * <li><b>From Account perspective</b>: Contains user information (userId,
 * userName, userEmail)</li>
 * <li><b>From User perspective</b>: Contains account information (accountId,
 * accountNumber, balance)</li>
 * </ul>
 * 
 * <p>
 * The {@code role} field is always populated and indicates the user's role on
 * the account
 * (PRIMARY or AUTHORIZED).
 * </p>
 * 
 * <p>
 * Null fields are automatically excluded from JSON serialization via
 * {@link JsonInclude}.
 * </p>
 * 
 * @see AccountResponse
 * @see UserResponse
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountUserDTO {

    // User information (populated when viewed from Account perspective)
    private Long userId;
    private String userName;
    private String userEmail;

    // Account information (populated when viewed from User perspective)
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;

    // Relationship information (always populated)
    private String role;

    public AccountUserDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Factory method for creating DTO from Account perspective.
     * Populates user information.
     * 
     * @param userId    the user ID
     * @param userName  the user name
     * @param userEmail the user email
     * @param role      the user's role on the account
     * @return AccountUserDTO with user information
     */
    public static AccountUserDTO fromAccountPerspective(Long userId, String userName,
            String userEmail, String role) {
        AccountUserDTO dto = new AccountUserDTO();
        dto.setUserId(userId);
        dto.setUserName(userName);
        dto.setUserEmail(userEmail);
        dto.setRole(role);
        return dto;
    }

    /**
     * Factory method for creating DTO from User perspective.
     * Populates account information.
     * 
     * @param accountId     the account ID
     * @param accountNumber the account number
     * @param balance       the account balance
     * @param role          the user's role on the account
     * @return AccountUserDTO with account information
     */
    public static AccountUserDTO fromUserPerspective(Long accountId, String accountNumber,
            BigDecimal balance, String role) {
        AccountUserDTO dto = new AccountUserDTO();
        dto.setAccountId(accountId);
        dto.setAccountNumber(accountNumber);
        dto.setBalance(balance);
        dto.setRole(role);
        return dto;
    }
}