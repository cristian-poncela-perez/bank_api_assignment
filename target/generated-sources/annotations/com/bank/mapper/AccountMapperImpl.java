package com.bank.mapper;

import com.bank.domain.Account;
import com.bank.domain.AccountUser;
import com.bank.domain.User;
import com.bank.dto.request.CreateAccountRequest;
import com.bank.dto.request.UpdateAccountRequest;
import com.bank.dto.response.AccountResponse;
import com.bank.dto.response.AccountUserDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-01T23:39:39+0200",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountResponse toResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setUsers( accountUserSetToAccountUserDTOList( account.getAccountUsers() ) );
        accountResponse.setId( account.getId() );
        accountResponse.setAccountNumber( account.getAccountNumber() );
        accountResponse.setBalance( account.getBalance() );

        return accountResponse;
    }

    @Override
    public List<AccountResponse> toResponseList(List<Account> accounts) {
        if ( accounts == null ) {
            return null;
        }

        List<AccountResponse> list = new ArrayList<AccountResponse>( accounts.size() );
        for ( Account account : accounts ) {
            list.add( toResponse( account ) );
        }

        return list;
    }

    @Override
    public AccountUserDTO accountUserToDTO(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }

        AccountUserDTO accountUserDTO = new AccountUserDTO();

        accountUserDTO.setUserId( accountUserUserId( accountUser ) );
        accountUserDTO.setUserName( accountUserUserName( accountUser ) );
        accountUserDTO.setUserEmail( accountUserUserEmail( accountUser ) );
        if ( accountUser.getRole() != null ) {
            accountUserDTO.setRole( accountUser.getRole().name() );
        }

        return accountUserDTO;
    }

    @Override
    public Account toEntity(CreateAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        Account account = new Account();

        account.setAccountNumber( request.getAccountNumber() );
        account.setBalance( request.getBalance() );

        return account;
    }

    @Override
    public void updateEntityFromRequest(UpdateAccountRequest request, Account account) {
        if ( request == null ) {
            return;
        }

        account.setAccountNumber( request.getAccountNumber() );
        account.setBalance( request.getBalance() );
    }

    protected List<AccountUserDTO> accountUserSetToAccountUserDTOList(Set<AccountUser> set) {
        if ( set == null ) {
            return null;
        }

        List<AccountUserDTO> list = new ArrayList<AccountUserDTO>( set.size() );
        for ( AccountUser accountUser : set ) {
            list.add( accountUserToDTO( accountUser ) );
        }

        return list;
    }

    private Long accountUserUserId(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        User user = accountUser.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String accountUserUserName(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        User user = accountUser.getUser();
        if ( user == null ) {
            return null;
        }
        String name = user.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String accountUserUserEmail(AccountUser accountUser) {
        if ( accountUser == null ) {
            return null;
        }
        User user = accountUser.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
