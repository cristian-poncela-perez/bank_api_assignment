package com.bank.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;
import com.bank.domain.User;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void countByBalanceGreaterThan_ReturnsCorrectCount() {
        // Given
        User user = new User("John Doe", "john.doe@example.com");
        entityManager.persist(user);

        Account account1 = new Account("ACC-001", new BigDecimal("1500.00"), user);
        Account account2 = new Account("ACC-002", new BigDecimal("500.00"), user);
        Account account3 = new Account("ACC-003", new BigDecimal("2000.00"), user);

        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.persist(account3);
        entityManager.flush();

        // When
        long count = accountRepository.countByBalanceGreaterThan(new BigDecimal("1000.00"));

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByBalanceLessThan_ReturnsCorrectCount() {
        // Given
        User user = new User("John Doe", "john.doe@example.com");
        entityManager.persist(user);

        Account account1 = new Account("ACC-001", new BigDecimal("100.00"), user);
        Account account2 = new Account("ACC-002", new BigDecimal("500.00"), user);
        Account account3 = new Account("ACC-003", new BigDecimal("1000.00"), user);

        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.persist(account3);
        entityManager.flush();

        // When
        long count = accountRepository.countByBalanceLessThan(new BigDecimal("600.00"));

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findAccountsByUserId_ReturnsUserAccounts() {
        // Given
        User primaryUser = new User("John Doe", "john.doe@example.com");
        User otherUser = new User("Jane Smith", "jane@example.com");
        entityManager.persist(primaryUser);
        entityManager.persist(otherUser);

        // Account 1: primaryUser is PRIMARY
        Account account1 = new Account("ACC-001", new BigDecimal("1000.00"), primaryUser);
        entityManager.persist(account1);

        // Account 2: otherUser is PRIMARY, primaryUser will be AUTHORIZED
        Account account2 = new Account("ACC-002", new BigDecimal("2000.00"), otherUser);
        entityManager.persist(account2);

        // Add primaryUser as AUTHORIZED on account2
        AccountUser au = new AccountUser(account2, primaryUser, AccountUserRole.AUTHORIZED);
        account2.getAccountUsers().add(au);
        primaryUser.getAccountUsers().add(au);

        entityManager.flush();

        // When - find accounts for primaryUser (should get both)
        List<Account> accounts = accountRepository.findAccountsByUserId(primaryUser.getId());

        // Then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).containsExactlyInAnyOrder(account1, account2);
    }
}