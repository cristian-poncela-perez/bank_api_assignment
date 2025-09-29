package com.bank.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Set;

import com.bank.exception.ErrorMessages;

import java.util.HashSet;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = ErrorMessages.ACCOUNT_NUMBER_REQUIRED)
    @Column(nullable = false, unique = true)
    private String accountNumber;

    @PositiveOrZero(message = ErrorMessages.BALANCE_NON_NEGATIVE)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AccountUser> accountUsers = new HashSet<>();

    public Account() {
    }

    public Account(String accountNumber, BigDecimal balance, User primaryUser) {
        this.accountNumber = accountNumber;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        AccountUser accountUser = new AccountUser(this, primaryUser, AccountUserRole.PRIMARY);
        this.accountUsers.add(accountUser);
        primaryUser.getAccountUsers().add(accountUser);
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

    public Set<AccountUser> getAccountUsers() {
        return accountUsers;
    }

    public void setAccountUsers(Set<AccountUser> accountUsers) {
        this.accountUsers = accountUsers;
    }

    public User getPrimaryUser() {
        return accountUsers.stream()
                .filter(au -> au.getRole() == AccountUserRole.PRIMARY)
                .map(AccountUser::getUser)
                .findFirst()
                .orElse(null);
    }

    public Set<User> getAuthorizedUsers() {
        Set<User> users = new HashSet<>();
        accountUsers.stream()
                .filter(au -> au.getRole() == AccountUserRole.AUTHORIZED)
                .forEach(au -> users.add(au.getUser()));
        return users;
    }
}