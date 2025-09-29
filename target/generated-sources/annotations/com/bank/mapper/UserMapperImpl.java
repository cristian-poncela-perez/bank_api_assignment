package com.bank.mapper;

import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.User;
import com.bank.dto.request.CreateUserRequest;
import com.bank.dto.request.UpdateUserRequest;
import com.bank.dto.response.AccountUserDTO;
import com.bank.dto.response.UserBalanceResponse;
import com.bank.dto.response.UserResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-01T23:39:39+0200",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setAccounts( accountUsersToOrderedList( user.getAccountUsers() ) );
        userResponse.setId( user.getId() );
        userResponse.setName( user.getName() );
        userResponse.setEmail( user.getEmail() );

        return userResponse;
    }

    @Override
    public List<UserResponse> toResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
        for ( User user : users ) {
            list.add( toResponse( user ) );
        }

        return list;
    }

    @Override
    public AccountUserDTO accountUserToDTO(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }

        AccountUserDTO accountUserDTO = new AccountUserDTO();

        accountUserDTO.setAccountId( accountUserAccountId( accountUser ) );
        accountUserDTO.setAccountNumber( accountUserAccountAccountNumber( accountUser ) );
        accountUserDTO.setBalance( accountUserAccountBalance( accountUser ) );
        if ( accountUser.getRole() != null ) {
            accountUserDTO.setRole( accountUser.getRole().name() );
        }

        return accountUserDTO;
    }

    @Override
    public User toEntity(CreateUserRequest request) {
        if ( request == null ) {
            return null;
        }

        User user = new User();

        user.setName( request.getName() );
        user.setEmail( request.getEmail() );

        return user;
    }

    @Override
    public void updateEntityFromRequest(UpdateUserRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            user.setName( request.getName() );
        }
        if ( request.getEmail() != null ) {
            user.setEmail( request.getEmail() );
        }
    }

    @Override
    public UserBalanceResponse toBalanceResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserBalanceResponse userBalanceResponse = new UserBalanceResponse();

        userBalanceResponse.setUserId( user.getId() );
        userBalanceResponse.setName( user.getName() );
        userBalanceResponse.setEmail( user.getEmail() );
        userBalanceResponse.setAccounts( toAccountSummaryList( user.getAccountUsers() ) );
        userBalanceResponse.setTotalBalance( calculateTotalBalance( user.getAccountUsers() ) );

        return userBalanceResponse;
    }

    private Long accountUserAccountId(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        Account account = accountUser.getAccount();
        if ( account == null ) {
            return null;
        }
        Long id = account.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String accountUserAccountAccountNumber(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        Account account = accountUser.getAccount();
        if ( account == null ) {
            return null;
        }
        String accountNumber = account.getAccountNumber();
        if ( accountNumber == null ) {
            return null;
        }
        return accountNumber;
    }

    private BigDecimal accountUserAccountBalance(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        Account account = accountUser.getAccount();
        if ( account == null ) {
            return null;
        }
        BigDecimal balance = account.getBalance();
        if ( balance == null ) {
            return null;
        }
        return balance;
    }
}
