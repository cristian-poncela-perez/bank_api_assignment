package com.bank.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.response.UserResponse;
import com.bank.service.UserService;
import com.bank.service.AccountService;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserBalanceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    private MockMvc mockMvc;
    private UserResponse testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create test user and accounts
        testUser = userService.createUser(new CreateUserRequest("John Doe", "john.doe@example.com"));

        // Create multiple accounts for the user
        accountService.createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));
        accountService.createAccount(new CreateAccountRequest("ACC-002", new BigDecimal("1500.50"), testUser.getId()));
        accountService.createAccount(new CreateAccountRequest("ACC-003", new BigDecimal("2000.25"), testUser.getId()));
    }

    @Test
    void getUserBalance_MultipleAccounts_ReturnsCorrectTotal() throws Exception {
        mockMvc.perform(get("/users/" + testUser.getId() + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalBalance").value(4500.75))
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts.length()").value(3))
                .andExpect(jsonPath("$.accounts[0].accountNumber").exists())
                .andExpect(jsonPath("$.accounts[0].balance").exists())
                .andExpect(jsonPath("$.accounts[0].role").exists());
    }

    @Test
    void getUserBalance_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/999/balance"))
                .andExpect(status().isNotFound());
    }
}