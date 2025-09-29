package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {

    /**
     * Finds all AccountUser associations for a given account ID and role.
     * 
     * @param accountId
     * @param role      role of the user in the account (e.g., PRIMARY, AUTHORIZED)
     * @return list of AccountUser associations
     */
    @Query("SELECT au FROM AccountUser au WHERE au.account.id = :accountId AND au.role = :role")
    List<AccountUser> findByAccountIdAndRole(@Param("accountId") Long accountId, @Param("role") AccountUserRole role);

    /**
     * Finds the AccountUser association for a specific account and user.
     * 
     * @param accountId
     * @param userId
     * @return optional AccountUser association
     */
    @Query("SELECT au FROM AccountUser au WHERE au.account.id = :accountId AND au.user.id = :userId")
    Optional<AccountUser> findByAccountIdAndUserId(@Param("accountId") Long accountId, @Param("userId") Long userId);

    /**
     * Counts the number of PRIMARY users associated with a specific account.
     * 
     * @param accountId
     * @return number of PRIMARY users
     */
    @Query("SELECT COUNT(au) FROM AccountUser au WHERE au.account.id = :accountId AND au.role = 'PRIMARY'")
    long countPrimaryUsersByAccountId(@Param("accountId") Long accountId);

    /**
     * Calculates the total balance across all accounts associated with a user.
     * Includes both PRIMARY and AUTHORIZED accounts.
     * 
     * @param userId ID of the user
     * @return total balance across all associated accounts
     */
    @Query("SELECT COALESCE(SUM(au.account.balance), 0) FROM AccountUser au WHERE au.user.id = :userId")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    /**
     * Counts the number of distinct accounts associated with a user.
     * Includes both PRIMARY and AUTHORIZED accounts.
     * 
     * @param userId ID of the user
     * @return number of distinct accounts
     */
    @Query("SELECT COUNT(DISTINCT au.account.id) FROM AccountUser au WHERE au.user.id = :userId")
    int countAccountsByUserId(@Param("userId") Long userId);
}