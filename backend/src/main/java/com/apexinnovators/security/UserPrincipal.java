package com.apexinnovators.security;

import com.apexinnovators.entity.Role;
import com.apexinnovators.entity.User;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authenticated principal: Spring Security user built from a DB account.
 * Carries the DB user id and role so services can enforce ownership rules and
 * record audit actors without re-loading the account.
 */
public class UserPrincipal extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final Role role;

    public UserPrincipal(User user) {
        super(user.getEmail(), user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        this.id = user.getId();
        this.role = user.getRole();
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }
}
