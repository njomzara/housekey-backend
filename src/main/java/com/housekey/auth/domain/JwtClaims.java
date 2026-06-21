package com.housekey.auth.domain;

import java.time.Instant;

import com.housekey.users.domain.UserRole;

public record JwtClaims(
        Long userId,
        String username,
        UserRole role,
        Instant expiresAt) {
}
