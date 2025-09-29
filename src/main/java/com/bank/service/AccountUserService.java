package com.bank.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.domain.AccountUser;
import com.bank.repository.AccountUserRepository;

@Service
@Transactional
public class AccountUserService {

    @Autowired
    private AccountUserRepository accountUserRepository;

    public Optional<AccountUser> findByAccountIdAndUserId(Long accountId, Long userId) {
        return accountUserRepository.findByAccountIdAndUserId(accountId, userId);
    }
}
