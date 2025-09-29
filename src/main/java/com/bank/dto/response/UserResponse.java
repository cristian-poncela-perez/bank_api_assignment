package com.bank.dto.response;

import java.util.List;

/**
 * Response DTO for User entity.
 * 
 * <p>
 * Includes associated accounts with their details and roles.
 * The accounts list contains {@link AccountUserDTO} instances with account
 * information.
 * </p>
 */
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private List<AccountUserDTO> accounts;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, List<AccountUserDTO> accounts) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.accounts = accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AccountUserDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountUserDTO> accounts) {
        this.accounts = accounts;
    }

}
