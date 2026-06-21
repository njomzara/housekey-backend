package com.housekey.auth.api;

import java.time.Instant;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserSummaryResponse user) {
}
