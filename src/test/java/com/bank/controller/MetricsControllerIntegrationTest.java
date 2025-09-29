package com.bank.controller;

import com.bank.domain.Account;
import com.bank.domain.User;
import com.bank.exception.ErrorMessages;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MetricsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser = userRepository.save(testUser);

        // Create test accounts with different balances
        createAccount("ACC-001", new BigDecimal("0"), testUser);
        createAccount("ACC-002", new BigDecimal("100"), testUser);
        createAccount("ACC-003", new BigDecimal("500"), testUser);
        createAccount("ACC-004", new BigDecimal("1500"), testUser);
        createAccount("ACC-005", new BigDecimal("2500"), testUser);
    }

    private Account createAccount(String accountNumber, BigDecimal balance, User user) {
        Account account = new Account(accountNumber, balance, user);
        return accountRepository.save(account);
    }

    // ========== GREATER THAN TESTS ==========

    @Test
    void getAccountMetrics_WithGreaterThan_ReturnsCorrectCount() throws Exception {
        // Accounts with balance > 100 (should be 3: ACC-003=500, ACC-004=1500,
        // ACC-005=2500)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(3)))
                .andExpect(jsonPath("$.condition", is("balance > 100")));
    }

    @Test
    void getAccountMetrics_WithGreaterThanHighValue_ReturnsZero() throws Exception {
        // Accounts with balance > 10000 (should be 0)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.condition", is("balance > 10000")));
    }

    @Test
    void getAccountMetrics_WithNegativeGreaterThan_ReturnsAllAccounts() throws Exception {
        // Accounts with balance > -100 (should be 5, all accounts)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)))
                .andExpect(jsonPath("$.condition", is("balance > -100")));
    }

    // ========== LESS THAN TESTS ==========

    @Test
    void getAccountMetrics_WithLessThan_ReturnsCorrectCount() throws Exception {
        // Accounts with balance < 500 (should be 2: ACC-001=0, ACC-002=100)
        mockMvc.perform(get("/metrics/accounts")
                .param("lessThan", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.condition", is("balance < 500")));
    }

    @Test
    void getAccountMetrics_WithNegativeLessThan_ReturnsZero() throws Exception {
        // Accounts with balance < -100 (should be 0, no accounts)
        mockMvc.perform(get("/metrics/accounts")
                .param("lessThan", "-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.condition", is("balance < -100")));
    }

    // ========== RANGE TESTS ==========

    @Test
    void getAccountMetrics_WithRange_ReturnsCorrectCount() throws Exception {
        // Accounts with 500 < balance < 2500 (should be 1: ACC-004=1500)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "500")
                .param("lessThan", "2500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.condition", is("balance > 500 AND balance < 2500")));
    }

    @Test
    void getAccountMetrics_WithWideRange_ReturnsCorrectCount() throws Exception {
        // Accounts with 0 < balance < 10000 (should be 4: ACC-002=100, ACC-003=500,
        // ACC-004=1500, ACC-005=2500)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "0")
                .param("lessThan", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(4)))
                .andExpect(jsonPath("$.condition", is("balance > 0 AND balance < 10000")));
    }

    @Test
    void getAccountMetrics_WithEmptyRange_ReturnsZero() throws Exception {
        // Accounts with 500 < balance < 501 (should be 0, no accounts in this narrow
        // range)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "500")
                .param("lessThan", "501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)));
    }

    @Test
    void getAccountMetrics_WithEqualGreaterThanAndLessThan_ReturnsZero() throws Exception {
        // Accounts with balance > 100 and balance < 100 (should be 0)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "100")
                .param("lessThan", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.condition", is("balance > 100 AND balance < 100")));
    }

    @Test
    void getAccountMetrics_WithImpossibleRange_ReturnsZero() throws Exception {
        // Accounts with balance > 1000 and balance < 100 (should be 0, impossible
        // range)
        mockMvc.perform(get("/metrics/accounts")
                .param("greaterThan", "1000")
                .param("lessThan", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.condition", is("balance > 1000 AND balance < 100")));
    }

    // ========== VALIDATION TESTS ==========

    @Test
    void getAccountMetrics_NoParameters_ReturnsBadRequest() throws Exception {
        // No parameters should cause an error
        mockMvc.perform(get("/metrics/accounts"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(ErrorMessages.METRICS_PARAMETERS_REQUIRED)));
    }
}