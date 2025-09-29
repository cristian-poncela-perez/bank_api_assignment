package com.bank.service;

import com.bank.domain.User;
import com.bank.domain.AccountUser;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserResponse;
import com.bank.dto.response.UserBalanceResponse;
import com.bank.exception.UserAlreadyExistsException;
import com.bank.exception.UserHasAccountsException;
import com.bank.exception.UserNotFoundException;
import com.bank.mapper.UserMapper;
import com.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com");
        testUser.setId(1L);
        testUser.setAccountUsers(new HashSet<>());

        testUserResponse = new UserResponse();
        testUserResponse.setId(1L);
        testUserResponse.setName("John Doe");
        testUserResponse.setEmail("john@example.com");

        createRequest = new CreateUserRequest("John Doe", "john@example.com");
        updateRequest = new UpdateUserRequest("Jane Doe", "jane@example.com");
    }

    @Test
    @DisplayName("Should find all users successfully")
    void shouldFindAllUsers() {
        // Given
        List<User> users = List.of(testUser);
        List<UserResponse> userResponses = List.of(testUserResponse);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseList(users)).thenReturn(userResponses);

        // When
        List<UserResponse> result = userService.findAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findAll();
        verify(userMapper).toResponseList(users);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindUserById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void shouldThrowUserNotFoundExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toEntity(createRequest)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.createUser(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).findByEmail(createRequest.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when email exists")
    void shouldThrowUserAlreadyExistsException() {
        // Given
        when(userRepository.findByEmail(createRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("john@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // When
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating to existing email")
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
        // Given
        User anotherUser = new User("Another", "jane@example.com");
        anotherUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updateRequest.getEmail())).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should delete user successfully when no accounts")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting user with accounts")
    void shouldThrowExceptionWhenDeletingUserWithAccounts() {
        // Given
        AccountUser accountUser = mock(AccountUser.class);
        testUser.getAccountUsers().add(accountUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserHasAccountsException.class)
                .hasMessageContaining("1");
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get user balance successfully")
    void shouldGetUserBalanceSuccessfully() {
        // Given
        UserBalanceResponse balanceResponse = new UserBalanceResponse();
        balanceResponse.setUserId(1L);
        balanceResponse.setTotalBalance(BigDecimal.valueOf(1000));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toBalanceResponse(testUser)).thenReturn(balanceResponse);

        // When
        UserBalanceResponse result = userService.getUserBalance(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}