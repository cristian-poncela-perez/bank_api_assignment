package com.bank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.bank.repository.AccountRepository;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.UserAlreadyAssociatedException;
import com.bank.exception.AccountAlreadyExistsException;
import com.bank.mapper.AccountMapper;
import com.bank.exception.AccountBalanceNotZeroException;
import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;
import com.bank.domain.User;
import com.bank.dto.response.AccountMetricsResponse;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.UpdateAccountRequest;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountUserService accountUserService;

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll() {
        List<Account> accounts = accountRepository.findAll();
        return accountMapper.toResponseList(accounts);
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(Long id) {
        Account account = getAccountOrThrow(id);
        return accountMapper.toResponse(account);
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        // Validate account number uniqueness
        if (accountRepository.findByAccountNumber(request.getAccountNumber()).isPresent()) {
            throw new AccountAlreadyExistsException(request.getAccountNumber());
        }
        User primaryUser = userService.getEntityById(request.getPrimaryUserId());
        Account account = new Account(request.getAccountNumber(), request.getBalance(), primaryUser);
        Account savedAccount = accountRepository.save(account);
        return accountMapper.toResponse(savedAccount);
    }

    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {
        Account account = getAccountOrThrow(id);

        // Validate account number uniqueness if it's being changed
        if (request.getAccountNumber() != null &&
                !request.getAccountNumber().equals(account.getAccountNumber())) {
            if (accountRepository.findByAccountNumber(request.getAccountNumber()).isPresent()) {
                throw new AccountAlreadyExistsException(request.getAccountNumber());
            }
        }

        accountMapper.updateEntityFromRequest(request, account);
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    public AccountResponse updateBalance(Long id, BigDecimal balance) {
        Account account = getAccountOrThrow(id);
        account.setBalance(balance);
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    public void deleteAccount(Long id) {
        Account account = getAccountOrThrow(id);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceNotZeroException(id);
        }

        accountRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AccountMetricsResponse getAccountMetrics(BigDecimal greaterThan, BigDecimal lessThan) {

        long count;
        String condition;

        // Case 1: Both parameters provided (range query)
        if (greaterThan != null && lessThan != null) {
            // Need to count accounts where greaterThan < balance < lessThan
            count = accountRepository.countByBalanceBetween(greaterThan, lessThan);
            condition = "balance > " + greaterThan + " AND balance < " + lessThan;
        }
        // Case 2: Only greaterThan provided
        else if (greaterThan != null) {
            count = accountRepository.countByBalanceGreaterThan(greaterThan);
            condition = "balance > " + greaterThan;
        }
        // Case 3: Only lessThan provided
        else {
            count = accountRepository.countByBalanceLessThan(lessThan);
            condition = "balance < " + lessThan;
        }

        return new AccountMetricsResponse(count, condition);
    }

    public AccountResponse addAuthorizedUser(Long id, Long userId) {
        if (accountUserService.findByAccountIdAndUserId(id, userId).isPresent()) {
            throw new UserAlreadyAssociatedException(id, userId);
        }
        Account account = getAccountOrThrow(id);
        User user = userService.getEntityById(userId);
        AccountUser accountUser = new AccountUser(account, user, AccountUserRole.AUTHORIZED);
        account.getAccountUsers().add(accountUser);
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    public AccountResponse removeAuthorizedUser(Long id, Long userId) {
        Assert.notNull(id, "Account ID must not be null");
        Assert.notNull(userId, "User ID must not be null");
        Account account = getAccountOrThrow(id);
        account.getAccountUsers()
                .removeIf(au -> au.getUser().getId().equals(userId) && au.getRole() == AccountUserRole.AUTHORIZED);
        Account updatedAccount = accountRepository.save(account);
        return accountMapper.toResponse(updatedAccount);
    }

    // Helper method
    private Account getAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}