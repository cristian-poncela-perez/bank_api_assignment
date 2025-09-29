package com.bank.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.bank.dto.request.AddAuthorizedUserRequest;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateAccountRequest;
import com.bank.dto.request.UpdateBalanceRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.UserResponse;
import com.bank.exception.ErrorMessages;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AccountControllerIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @Autowired
        private UserService userService;

        @Autowired
        private AccountService accountService;

        @Autowired
        private ObjectMapper objectMapper;

        private MockMvc mockMvc;
        private UserResponse testUser;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
                testUser = userService.createUser(new CreateUserRequest("John Doe", "john.doe@example.com"));
        }

        // ========== GET ALL ACCOUNTS ==========

        @Test
        void getAllAccounts_ReturnsAccountsList() throws Exception {
                accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));
                accountService.createAccount(
                                new CreateAccountRequest("ACC-002", new BigDecimal("2000.00"), testUser.getId()));

                mockMvc.perform(get("/accounts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].accountNumber").exists())
                                .andExpect(jsonPath("$[1].accountNumber").exists());
        }

        // ========== GET ACCOUNT BY ID ==========

        @Test
        void getAccountById_ExistingAccount_ReturnsAccount() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                mockMvc.perform(get("/accounts/" + account.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(account.getId()))
                                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                                .andExpect(jsonPath("$.balance").value(1000.00));
        }

        @Test
        void getAccountById_NonExistingAccount_ReturnsNotFound() throws Exception {
                mockMvc.perform(get("/accounts/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value(
                                                String.format(ErrorMessages.ACCOUNT_NOT_FOUND, 999L)));
        }

        // ========== CREATE ACCOUNT ==========

        @Test
        void createAccount_ValidAccount_ReturnsCreated() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"),
                                testUser.getId());

                mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                                .andExpect(jsonPath("$.balance").value(1000.00));
        }

        @Test
        void createAccount_NegativeBalance_ReturnsBadRequest() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest("ACC-001", new BigDecimal("-100.00"),
                                testUser.getId());

                mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.balance").exists());
        }

        @Test
        void createAccount_NonExistingUser_ReturnsNotFound() throws Exception {
                CreateAccountRequest request = new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), 999L);

                mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createAccount_DuplicateAccountNumber_ReturnsConflict() throws Exception {
                accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                CreateAccountRequest duplicate = new CreateAccountRequest("ACC-001", new BigDecimal("2000.00"),
                                testUser.getId());

                mockMvc.perform(post("/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(duplicate)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value(
                                                String.format(ErrorMessages.ACCOUNT_ALREADY_EXISTS, "ACC-001")));
        }

        // ========== UPDATE ACCOUNT ==========

        @Test
        void updateAccount_ValidUpdate_ReturnsUpdatedAccount() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                UpdateAccountRequest updateRequest = new UpdateAccountRequest("ACC-001-UPDATED",
                                new BigDecimal("1500.00"));

                mockMvc.perform(put("/accounts/" + account.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accountNumber").value("ACC-001-UPDATED"))
                                .andExpect(jsonPath("$.balance").value(1500.00));
        }

        @Test
        void updateAccount_DuplicateAccountNumber_ReturnsConflict() throws Exception {
                AccountResponse account1 = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));
                accountService.createAccount(
                                new CreateAccountRequest("ACC-002", new BigDecimal("2000.00"), testUser.getId()));

                UpdateAccountRequest updateRequest = new UpdateAccountRequest("ACC-002", new BigDecimal("1500.00"));

                mockMvc.perform(put("/accounts/" + account1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value(
                                                String.format(ErrorMessages.ACCOUNT_ALREADY_EXISTS, "ACC-002")));
        }

        @Test
        void updateAccount_NonExistingAccount_ReturnsNotFound() throws Exception {
                UpdateAccountRequest updateRequest = new UpdateAccountRequest("ACC-999", new BigDecimal("1000.00"));

                mockMvc.perform(put("/accounts/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isNotFound());
        }

        // ========== UPDATE BALANCE ==========

        @Test
        void updateBalance_ValidBalance_ReturnsUpdatedAccount() throws Exception {
                AccountResponse account = accountService
                                .createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"),
                                                testUser.getId()));

                UpdateBalanceRequest updateRequest = new UpdateBalanceRequest(new BigDecimal("2000.00"));

                mockMvc.perform(patch("/accounts/" + account.getId() + "/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.balance").value(2000.00));
        }

        @Test
        void updateBalance_NegativeBalance_ReturnsBadRequest() throws Exception {
                AccountResponse account = accountService
                                .createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"),
                                                testUser.getId()));

                UpdateBalanceRequest updateRequest = new UpdateBalanceRequest(new BigDecimal("-500.00"));

                mockMvc.perform(patch("/accounts/" + account.getId() + "/balance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors.balance").exists());
        }

        // ========== DELETE ACCOUNT ==========

        @Test
        void deleteAccount_ZeroBalance_ReturnsOk() throws Exception {
                AccountResponse account = accountService
                                .createAccount(new CreateAccountRequest("ACC-001", BigDecimal.ZERO,
                                                testUser.getId()));

                mockMvc.perform(delete("/accounts/" + account.getId()))
                                .andExpect(status().isOk());
        }

        @Test
        void deleteAccount_NonZeroBalance_ReturnsConflict() throws Exception {
                AccountResponse account = accountService
                                .createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"),
                                                testUser.getId()));

                mockMvc.perform(delete("/accounts/" + account.getId()))
                                .andExpect(status().isConflict());
        }

        @Test
        void deleteAccount_NonExistingAccount_ReturnsNotFound() throws Exception {
                mockMvc.perform(delete("/accounts/999"))
                                .andExpect(status().isNotFound());
        }

        // ========== ADD AUTHORIZED USER ==========

        @Test
        void addAuthorizedUser_ValidUser_ReturnsUpdatedAccount() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                UserResponse authorizedUser = userService.createUser(
                                new CreateUserRequest("Jane Doe", "jane@example.com"));

                AddAuthorizedUserRequest request = new AddAuthorizedUserRequest(authorizedUser.getId());

                mockMvc.perform(post("/accounts/" + account.getId() + "/authorized-users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(account.getId()));
        }

        @Test
        void addAuthorizedUser_NonExistingUser_ReturnsNotFound() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                AddAuthorizedUserRequest request = new AddAuthorizedUserRequest(999L);

                mockMvc.perform(post("/accounts/" + account.getId() + "/authorized-users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void addAuthorizedUser_NonExistingAccount_ReturnsNotFound() throws Exception {
                AddAuthorizedUserRequest request = new AddAuthorizedUserRequest(testUser.getId());

                mockMvc.perform(post("/accounts/999/authorized-users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void addAuthorizedUser_MissingUserId_ReturnsBadRequest() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                // Request with null userId
                mockMvc.perform(post("/accounts/" + account.getId() + "/authorized-users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        // ========== REMOVE AUTHORIZED USER ==========

        @Test
        void removeAuthorizedUser_ValidUser_ReturnsUpdatedAccount() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                UserResponse authorizedUser = userService.createUser(
                                new CreateUserRequest("Jane Doe", "jane@example.com"));

                accountService.addAuthorizedUser(account.getId(), authorizedUser.getId());

                mockMvc.perform(delete("/accounts/" + account.getId() + "/authorized-users/" + authorizedUser.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(account.getId()));
        }

        @Test
        void removeAuthorizedUser_NonExistingAccount_ReturnsNotFound() throws Exception {
                mockMvc.perform(delete("/accounts/999/authorized-users/" + testUser.getId()))
                                .andExpect(status().isNotFound());
        }

        @Test
        void removeAuthorizedUser_NonExistingUser_Success() throws Exception {
                AccountResponse account = accountService.createAccount(
                                new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), testUser.getId()));

                // Removing a user that was never added should succeed (idempotent)
                mockMvc.perform(delete("/accounts/" + account.getId() + "/authorized-users/999"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(account.getId()));
        }
}