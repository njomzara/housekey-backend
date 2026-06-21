package com.housekey.auth.api;

import java.util.List;

import com.housekey.users.domain.UserRole;
import com.housekey.users.infrastructure.UserEntity;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        List<String> roles,
        String firstName,
        String lastName,
        String phone) {

    public static UserSummaryResponse from(UserEntity user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                List.of(user.getRole().name()),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone());
    }
}
