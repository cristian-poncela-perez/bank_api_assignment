package com.bank.mapper;

import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.AccountUserRole;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.UpdateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.AccountUserDTO;
import org.mapstruct.*;
import java.util.Set;
import java.util.Comparator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Account entity and DTOs.
 * Handles bidirectional mapping and includes custom logic for sorting account
 * users.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Converts Account entity to AccountResponse DTO.
     * 
     * @param account the account entity
     * @return account response with associated users
     */
    @Mapping(source = "accountUsers", target = "users")
    AccountResponse toResponse(Account account);

    /**
     * Converts a list of Account entities to AccountResponse DTOs.
     * 
     * @param accounts list of account entities
     * @return list of account responses
     */
    List<AccountResponse> toResponseList(List<Account> accounts);

    /**
     * Converts AccountUser to AccountUserDTO from account perspective.
     * Only populates user information; account fields are ignored.
     * 
     * @param accountUser the account-user association
     * @return DTO with user details and role
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "role", target = "role")
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", ignore = true)
    AccountUserDTO accountUserToDTO(AccountUser accountUser);

    /**
     * Converts CreateAccountRequest to Account entity.
     * The primary user and account users must be set in the service layer.
     * 
     * @param request the create account request
     * @return new account entity (without associations)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountUsers", ignore = true)
    Account toEntity(CreateAccountRequest request);

    /**
     * Converts and sorts account users for response.
     * PRIMARY users appear first, then AUTHORIZED users, sorted by user ID within
     * each group.
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
                        .thenComparing(au -> au.getUser().getId()))
                .map(this::accountUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing Account entity from UpdateAccountRequest.
     * Only modifies fields present in the request; associations are not updated.
     * 
     * @param request the update request
     * @param account the account entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountUsers", ignore = true)
    void updateEntityFromRequest(UpdateAccountRequest request, @MappingTarget Account account);
}