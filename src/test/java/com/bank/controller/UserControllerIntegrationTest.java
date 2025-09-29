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

import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserResponse;
import com.bank.exception.ErrorMessages;
import com.bank.service.AccountService;
import com.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private CreateUserRequest testCreateUserRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        testCreateUserRequest = new CreateUserRequest("John Doe", "john.doe@example.com");
    }

    // ========== GET ALL USERS ==========

    @Test
    void getAllUsers_ReturnsUsersList() throws Exception {
        userService.createUser(testCreateUserRequest);
        userService.createUser(new CreateUserRequest("Jane Smith", "jane@example.com"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    // ========== GET USER BY ID ==========

    @Test
    void getUserById_ExistingUser_ReturnsUser() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        mockMvc.perform(get("/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getUserById_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        String.format(ErrorMessages.USER_NOT_FOUND, 999L)));
    }

    // ========== CREATE USER ==========

    @Test
    void createUser_ValidUser_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        testCreateUserRequest.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createUser_MissingName_ReturnsBadRequest() throws Exception {
        testCreateUserRequest.setName("");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createUser_MissingEmail_ReturnsBadRequest() throws Exception {
        testCreateUserRequest.setEmail("");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        userService.createUser(testCreateUserRequest);

        CreateUserRequest duplicateUser = new CreateUserRequest("Jane Doe", "john.doe@example.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format(ErrorMessages.USER_ALREADY_EXISTS, "john.doe@example.com")));
    }

    // ========== EMAIL CASE SENSITIVITY TESTS ==========

    @Test
    void createUser_UppercaseEmail_SavesAsLowercase() throws Exception {
        CreateUserRequest request = new CreateUserRequest("John Doe", "JOHN.DOE@EXAMPLE.COM");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void createUser_MixedCaseEmail_SavesAsLowercase() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Jane Smith", "Jane.Smith@Example.COM");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));
    }

    @Test
    void createUser_DuplicateEmailDifferentCase_ReturnsConflict() throws Exception {
        userService.createUser(new CreateUserRequest("John", "john@example.com"));

        CreateUserRequest duplicate = new CreateUserRequest("Jane", "JOHN@EXAMPLE.COM");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format(ErrorMessages.USER_ALREADY_EXISTS, "john@example.com")));
    }

    @Test
    void createUser_EmailWithWhitespace_TrimsAndLowercases() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Test User", "  TEST@EXAMPLE.COM  ");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    // ========== UPDATE USER ==========

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest("John Updated", "john.updated@example.com");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    void updateUser_UppercaseEmail_SavesAsLowercase() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest("John Updated", "JOHN.UPDATED@EXAMPLE.COM");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    void updateUser_DuplicateEmailDifferentCase_ReturnsConflict() throws Exception {
        UserResponse user1 = userService.createUser(new CreateUserRequest("User 1", "user1@example.com"));
        userService.createUser(new CreateUserRequest("User 2", "user2@example.com"));

        UpdateUserRequest updateRequest = new UpdateUserRequest("User 1", "USER2@EXAMPLE.COM");

        mockMvc.perform(put("/users/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest("John Updated", "invalid-email");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void updateUser_MissingName_ReturnsBadRequest() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest("", "john.updated@example.com");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_MissingEmail_ReturnsBadRequest() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest("John Updated", "");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_NonExistingUser_ReturnsNotFound() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest("John Updated", "john.updated@example.com");

        mockMvc.perform(put("/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE USER ==========

    @Test
    void deleteUser_ExistingUserWithoutAccounts_ReturnsOk() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        mockMvc.perform(delete("/users/" + createdUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_UserWithAccounts_ReturnsConflict() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);
        accountService.createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"),
                createdUser.getId()));

        mockMvc.perform(delete("/users/" + createdUser.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format(ErrorMessages.USER_HAS_ACCOUNTS, createdUser.getId())));
    }

    // ========== GET USER BALANCE ==========

    @Test
    void getUserBalance_ExistingUser_ReturnsBalance() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);
        accountService
                .createAccount(new CreateAccountRequest("ACC-001", new BigDecimal("1000.00"), createdUser.getId()));
        accountService
                .createAccount(new CreateAccountRequest("ACC-002", new BigDecimal("1500.50"), createdUser.getId()));

        mockMvc.perform(get("/users/" + createdUser.getId() + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.totalBalance").value(2500.50))
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts.length()").value(2));
    }

    @Test
    void getUserBalance_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/999/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBalance_UserWithNoAccounts_ReturnsZeroBalance() throws Exception {
        UserResponse createdUser = userService.createUser(testCreateUserRequest);

        mockMvc.perform(get("/users/" + createdUser.getId() + "/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(createdUser.getId()))
                .andExpect(jsonPath("$.totalBalance").value(0))
                .andExpect(jsonPath("$.accounts").isEmpty());
    }
}