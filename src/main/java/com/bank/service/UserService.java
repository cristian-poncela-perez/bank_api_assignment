package com.bank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.bank.repository.UserRepository;
import com.bank.exception.UserNotFoundException;
import com.bank.mapper.UserMapper;
import com.bank.exception.UserAlreadyExistsException;
import com.bank.exception.UserHasAccountsException;
import com.bank.domain.User;
import com.bank.dto.response.UserBalanceResponse;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserResponse;

import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = getUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        // Normalize email for lookup (case-insensitive check)
        String normalizedEmail = normalizeEmail(request.getEmail());

        // Validate email uniqueness
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException(normalizedEmail);
        }
        User user = userRepository.save(userMapper.toEntity(request));
        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User existingUser = getUserOrThrow(id);

        // Normalize email for comparison (case-insensitive check)
        String normalizedEmail = normalizeEmail(request.getEmail());

        // Check if email is being updated to one of another user
        if (!existingUser.getEmail().equals(normalizedEmail) &&
                userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException(normalizedEmail);
        }
        existingUser.setName(request.getName());
        existingUser.setEmail(request.getEmail()); // Setter will normalize it
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);

        if (!user.getAccountUsers().isEmpty()) {
            throw new UserHasAccountsException(id);
        }

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public UserBalanceResponse getUserBalance(Long id) {
        User user = getUserOrThrow(id);
        return userMapper.toBalanceResponse(user);
    }

    // Helper methods
    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : null;
    }

    // For internal use by other services
    public User getEntityById(Long id) {
        return getUserOrThrow(id);
    }
}