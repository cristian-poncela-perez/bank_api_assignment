package com.bank.mapper;

import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;
import com.bank.domain.User;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserBalanceResponse;
import com.bank.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;

    // Users
    private User userWithPrimaryAccounts;
    private User userWithMixedAccounts;
    private User userWithAuthorizedOnly;
    private User userWithNoAccounts;

    // Accounts
    private Account account1;
    private Account account2;
    private Account account3;
    private Account account4;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);

        // User 1: Has 2 PRIMARY accounts
        userWithPrimaryAccounts = new User("Alice Primary", "alice@example.com");
        userWithPrimaryAccounts.setId(1L);
        userWithPrimaryAccounts.setAccountUsers(new HashSet<>());

        account1 = new Account("ACC-001", new BigDecimal("1000.00"), userWithPrimaryAccounts);
        account1.setId(1L);

        account2 = new Account("ACC-002", new BigDecimal("2000.00"), userWithPrimaryAccounts);
        account2.setId(2L);

        // User 2: Has 1 PRIMARY and 1 AUTHORIZED account
        userWithMixedAccounts = new User("Bob Mixed", "bob@example.com");
        userWithMixedAccounts.setId(2L);
        userWithMixedAccounts.setAccountUsers(new HashSet<>());

        account3 = new Account("ACC-003", new BigDecimal("1500.00"), userWithMixedAccounts);
        account3.setId(3L);

        // Add userWithMixedAccounts as AUTHORIZED on account1
        AccountUser authorizedLink = new AccountUser(account1, userWithMixedAccounts, AccountUserRole.AUTHORIZED);
        account1.getAccountUsers().add(authorizedLink);
        userWithMixedAccounts.getAccountUsers().add(authorizedLink);

        // User 3: Only AUTHORIZED accounts (no PRIMARY)
        userWithAuthorizedOnly = new User("Charlie Auth", "charlie@example.com");
        userWithAuthorizedOnly.setId(3L);
        userWithAuthorizedOnly.setAccountUsers(new HashSet<>());

        account4 = new Account("ACC-004", new BigDecimal("500.00"), userWithPrimaryAccounts);
        account4.setId(4L);

        // Add userWithAuthorizedOnly as AUTHORIZED on account2 and account3
        AccountUser auth1 = new AccountUser(account2, userWithAuthorizedOnly, AccountUserRole.AUTHORIZED);
        account2.getAccountUsers().add(auth1);
        userWithAuthorizedOnly.getAccountUsers().add(auth1);

        AccountUser auth2 = new AccountUser(account3, userWithAuthorizedOnly, AccountUserRole.AUTHORIZED);
        account3.getAccountUsers().add(auth2);
        userWithAuthorizedOnly.getAccountUsers().add(auth2);

        // User 4: No accounts at all
        userWithNoAccounts = new User("Diana None", "diana@example.com");
        userWithNoAccounts.setId(4L);
        userWithNoAccounts.setAccountUsers(new HashSet<>());
    }

    @Test
    @DisplayName("Should map User to UserResponse")
    void shouldMapUserToUserResponse() {
        UserResponse response = userMapper.toResponse(userWithPrimaryAccounts);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Primary");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should map List of Users to List of UserResponses")
    void shouldMapUserListToResponseList() {
        List<User> users = List.of(userWithPrimaryAccounts, userWithMixedAccounts);

        List<UserResponse> responses = userMapper.toResponseList(users);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Alice Primary");
        assertThat(responses.get(1).getName()).isEqualTo("Bob Mixed");
    }

    @Test
    @DisplayName("Should map CreateUserRequest to User entity")
    void shouldMapCreateRequestToEntity() {
        CreateUserRequest request = new CreateUserRequest("New User", "new@example.com");

        User user = userMapper.toEntity(request);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("New User");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getId()).isNull();
    }

    @Test
    @DisplayName("Should update User entity from UpdateUserRequest")
    void shouldUpdateEntityFromRequest() {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@example.com");

        userMapper.updateEntityFromRequest(request, userWithNoAccounts);

        assertThat(userWithNoAccounts.getName()).isEqualTo("Updated Name");
        assertThat(userWithNoAccounts.getEmail()).isEqualTo("updated@example.com");
        assertThat(userWithNoAccounts.getId()).isEqualTo(4L); // ID should remain unchanged
    }

    @Test
    @DisplayName("Should map user with PRIMARY accounts only")
    void shouldMapUserWithPrimaryAccountsOnly() {
        UserBalanceResponse response = userMapper.toBalanceResponse(userWithPrimaryAccounts);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Alice Primary");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        // 3 PRIMARY accounts: ACC-001 (1000) + ACC-002 (2000) + ACC-004 (500) = 3500
        assertThat(response.getTotalBalance()).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(response.getAccounts()).hasSize(3);
        assertThat(response.getAccounts()).allMatch(acc -> acc.getRole().equals("PRIMARY"));
    }

    @Test
    @DisplayName("Should map user with mixed PRIMARY and AUTHORIZED accounts")
    void shouldMapUserWithMixedAccounts() {
        UserBalanceResponse response = userMapper.toBalanceResponse(userWithMixedAccounts);

        assertThat(response.getTotalBalance()).isEqualByComparingTo(new BigDecimal("2500.00"));
        // 1 PRIMARY (ACC-003: 1500) + 1 AUTHORIZED (ACC-001: 1000) = 2500
        assertThat(response.getAccounts()).hasSize(2);

        // Verify PRIMARY comes first
        assertThat(response.getAccounts().get(0).getRole()).isEqualTo("PRIMARY");
        assertThat(response.getAccounts().get(0).getAccountId()).isEqualTo(3L);
        assertThat(response.getAccounts().get(0).getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Should map user with AUTHORIZED accounts only")
    void shouldMapUserWithAuthorizedAccountsOnly() {
        UserBalanceResponse response = userMapper.toBalanceResponse(userWithAuthorizedOnly);

        // 2 AUTHORIZED accounts: ACC-002 (2000) + ACC-003 (1500) = 3500
        assertThat(response.getTotalBalance()).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(response.getAccounts()).hasSize(2);
        assertThat(response.getAccounts()).allMatch(acc -> acc.getRole().equals("AUTHORIZED"));
    }

    @Test
    @DisplayName("Should order accounts with PRIMARY role first")
    void shouldOrderAccountsWithPrimaryFirst() {
        UserBalanceResponse response = userMapper.toBalanceResponse(userWithMixedAccounts);

        // Should have PRIMARY first, then AUTHORIZED
        assertThat(response.getAccounts()).hasSize(2);
        assertThat(response.getAccounts().get(0).getRole()).isEqualTo("PRIMARY");
        assertThat(response.getAccounts().get(0).getAccountId()).isEqualTo(3L); // ACC-003
        assertThat(response.getAccounts().get(1).getRole()).isEqualTo("AUTHORIZED");
        assertThat(response.getAccounts().get(1).getAccountId()).isEqualTo(1L); // ACC-001
    }

    @Test
    @DisplayName("Should handle user with no accounts")
    void shouldHandleUserWithNoAccounts() {
        UserBalanceResponse response = userMapper.toBalanceResponse(userWithNoAccounts);

        assertThat(response.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getAccounts()).isEmpty();
    }

    @Test
    @DisplayName("Should map AccountUser set to AccountSummary list")
    void shouldMapAccountUserToAccountSummary() {
        List<UserBalanceResponse.AccountSummary> summaries = userMapper
                .toAccountSummaryList(userWithMixedAccounts.getAccountUsers());

        assertThat(summaries).hasSize(2);
        // First should be PRIMARY
        assertThat(summaries.get(0).getRole()).isEqualTo("PRIMARY");
        assertThat(summaries.get(0).getAccountId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should calculate total balance correctly for multiple accounts")
    void shouldCalculateTotalBalance() {
        // userWithPrimaryAccounts has 3 accounts: 1000 + 2000 + 500 = 3500
        BigDecimal total = userMapper.calculateTotalBalance(userWithPrimaryAccounts.getAccountUsers());

        assertThat(total).isEqualByComparingTo(new BigDecimal("3500.00"));
    }

    @Test
    @DisplayName("Should calculate zero balance for user with no accounts")
    void shouldCalculateZeroBalanceForNoAccounts() {
        BigDecimal total = userMapper.calculateTotalBalance(userWithNoAccounts.getAccountUsers());

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }
}