package com.bank.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for the GET /users/{id}/balance endpoint.
 * Returns user details along with total balance across all accounts.
 */
public class UserBalanceResponse {

    private Long userId;
    private String name;
    private String email;
    private BigDecimal totalBalance;
    private List<AccountSummary> accounts;

    public UserBalanceResponse() {
    }

    public UserBalanceResponse(Long userId, String name, String email, BigDecimal totalBalance,
            List<AccountSummary> accounts) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.totalBalance = totalBalance;
        this.accounts = accounts;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public List<AccountSummary> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountSummary> accounts) {
        this.accounts = accounts;
    }

    /**
     * Summary of each account the user has access to.
     */
    public static class AccountSummary {
        private Long accountId;
        private String accountNumber;
        private BigDecimal balance;
        private String role;

        public AccountSummary() {
        }

        public AccountSummary(Long accountId, String accountNumber, BigDecimal balance, String role) {
            this.accountId = accountId;
            this.accountNumber = accountNumber;
            this.balance = balance;
            this.role = role;
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
    }

}
