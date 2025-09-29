package com.bank.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for Account entity.
 * 
 * <p>
 * Includes associated users with their details and roles.
 * The users list contains {@link AccountUserDTO} instances with user
 * information.
 * </p>
 */
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private List<AccountUserDTO> users;

    public AccountResponse() {
    }

    public AccountResponse(Long id, String accountNumber, BigDecimal balance, List<AccountUserDTO> users) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<AccountUserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<AccountUserDTO> users) {
        this.users = users;
    }

}
