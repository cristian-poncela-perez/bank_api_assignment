package com.bank.mapper;

import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;
import com.bank.domain.User;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.UserResponse;
import com.bank.dto.response.UserBalanceResponse;
import com.bank.dto.response.AccountUserDTO;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity and DTOs.
 * Handles conversion to various response formats including balance
 * calculations.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts User entity to UserResponse DTO.
     * 
     * @param user the user entity
     * @return user response with associated accounts
     */
    @Mapping(source = "accountUsers", target = "accounts", qualifiedByName = "accountUsersToOrderedList")
    UserResponse toResponse(User user);

    /**
     * Converts a list of User entities to UserResponse DTOs.
     * 
     * @param users list of user entities
     * @return list of user responses
     */
    List<UserResponse> toResponseList(List<User> users);

    /**
     * Converts AccountUser to AccountUserDTO from user perspective.
     * Only populates account information; user fields are ignored.
     * 
     * @param accountUser the account-user association
     * @return DTO with account details and role
     */
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.accountNumber", target = "accountNumber")
    @Mapping(source = "account.balance", target = "balance")
    @Mapping(source = "role", target = "role")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    AccountUserDTO accountUserToDTO(AccountUser accountUser);

    /**
     * Converts and sorts account users for response.
     * Accounts where the user is the PRIMARY user appear first, then accounts where
     * they are AUTHORIZED, sorted by account ID within each group.
     * 
     * @param accountUsers set of account-user associations
     * @return ordered list of account user DTOs
     */
    @Named("accountUsersToOrderedList")
    default List<AccountUserDTO> accountUsersToOrderedList(Set<AccountUser> accountUsers) {
        if (accountUsers == null || accountUsers.isEmpty()) {
            return List.of();
        }

        return accountUsers.stream()
                .sorted(Comparator
                        .comparing((AccountUser au) -> au.getRole() == AccountUserRole.PRIMARY ? 0 : 1)
                        .thenComparing(au -> au.getAccount().getId()))
                .map(this::accountUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts CreateUserRequest to User entity.
     * 
     * @param request the create user request
     * @return new user entity (without associations)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountUsers", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Updates an existing User entity from UpdateUserRequest.
     * Only updates non-null fields; associations are not modified.
     * 
     * @param request the update request
     * @param user    the user entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountUsers", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    /**
     * Converts User entity to UserBalanceResponse with aggregated balance.
     * 
     * @param user the user entity
     * @return balance response with total across all accounts and account details
     */
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "accountUsers", target = "accounts", qualifiedByName = "toAccountSummaryList")
    @Mapping(source = "accountUsers", target = "totalBalance", qualifiedByName = "calculateTotalBalance")
    UserBalanceResponse toBalanceResponse(User user);

    /**
     * Converts account users to ordered account summary list.
     * Accounts where the user is the PRIMARY user appear first, then accounts where
     * they are AUTHORIZED, sorted by account ID within each group.
     * 
     * @param accountUsers set of account-user associations
     * @return ordered list of account summaries
     */
    @Named("toAccountSummaryList")
    default List<UserBalanceResponse.AccountSummary> toAccountSummaryList(Set<AccountUser> accountUsers) {
        if (accountUsers == null || accountUsers.isEmpty()) {
            return List.of();
        }

        return accountUsers.stream()
                .sorted(Comparator
                        .comparing((AccountUser au) -> au.getRole() == AccountUserRole.PRIMARY ? 0 : 1)
                        .thenComparing(au -> au.getAccount().getId()))
                .map(au -> new UserBalanceResponse.AccountSummary(
                        au.getAccount().getId(),
                        au.getAccount().getAccountNumber(),
                        au.getAccount().getBalance(),
                        au.getRole().name()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates total balance across all user's accounts.
     * Includes both PRIMARY and AUTHORIZED accounts.
     * 
     * @param accountUsers set of account-user associations
     * @return sum of all account balances, or zero if no accounts
     */
    @Named("calculateTotalBalance")
    default BigDecimal calculateTotalBalance(Set<AccountUser> accountUsers) {
        if (accountUsers == null || accountUsers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return accountUsers.stream()
                .map(au -> au.getAccount().getBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}