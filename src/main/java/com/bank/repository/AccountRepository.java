package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bank.domain.Account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find an account by its account number
     * 
     * @param accountNumber the account number to search for
     * @return an Optional containing the account if found, or empty if not found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts associated with a specific user (any role)
     * 
     * @param userId ID of the user
     * @return list of accounts associated with the user
     */
    @Query("SELECT a FROM Account a JOIN a.accountUsers au WHERE au.user.id = :userId")
    List<Account> findAccountsByUserId(@Param("userId") Long userId);

    /**
     * Find all accounts associated with a specific user and role
     * 
     * @param userId ID of the user
     * @param role   role of the user in the account (e.g., PRIMARY, AUTHORIZED)
     * @return list of accounts associated with the user and role
     */
    @Query("SELECT a FROM Account a JOIN a.accountUsers au WHERE au.user.id = :userId AND au.role = :role")
    List<Account> findAccountsByUserIdAndRole(@Param("userId") Long userId,
            @Param("role") com.bank.domain.AccountUserRole role);

    /**
     * Count accounts with balance greater than the specified amount
     * 
     * @param balance lower bound (exclusive)
     * @return number of accounts with balance greater than the specified amount
     */
    long countByBalanceGreaterThan(BigDecimal balance);

    /**
     * Count accounts with balance less than the specified amount
     * 
     * @param balance upper bound (exclusive)
     * @return number of accounts with balance less than the specified amount
     */
    long countByBalanceLessThan(BigDecimal balance);

    /**
     * Count accounts with balance between the specified amounts (exclusive)
     * 
     * @param greaterThan lower bound (exclusive)
     * @param lessThan    upper bound (exclusive)
     * @return number of accounts with balance between the specified amounts
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.balance > :greaterThan AND a.balance < :lessThan")
    long countByBalanceBetween(@Param("greaterThan") BigDecimal greaterThan, @Param("lessThan") BigDecimal lessThan);

}