package com.bank.domain;

import java.util.Objects;
import jakarta.persistence.*;

@Entity
@Table(name = "account_users", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "account_id", "user_id" })
})
public class AccountUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountUserRole role;

    // Default constructor for JPA
    public AccountUser() {
    }

    public AccountUser(Account account, User user, AccountUserRole role) {
        this.account = account;
        this.user = user;
        this.role = role;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AccountUserRole getRole() {
        return role;
    }

    public void setRole(AccountUserRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AccountUser))
            return false;
        AccountUser that = (AccountUser) o;
        return Objects.equals(account.getId(), that.account.getId()) &&
                Objects.equals(user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(account.getId(), user.getId());
    }
}
