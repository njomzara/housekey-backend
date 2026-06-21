package com.housekey.users.api;

import java.time.LocalDateTime;
import java.util.List;

import com.housekey.users.domain.UserRole;
import com.housekey.users.infrastructure.UserEntity;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        List<String> roles,
        String firstName,
        String lastName,
        String phone,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CurrentUserResponse from(UserEntity user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                List.of(user.getRole().name()),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
