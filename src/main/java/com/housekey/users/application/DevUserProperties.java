package com.housekey.users.application;

import com.housekey.users.domain.UserRole;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "housekey.dev-user")
public record DevUserProperties(
        boolean enabled,
        String username,
        String email,
        String password,
        UserRole role,
        String firstName,
        String lastName,
        String phone) {

    public DevUserProperties {
        if (role == null) {
            role = UserRole.AGENCY;
        }
    }
}
