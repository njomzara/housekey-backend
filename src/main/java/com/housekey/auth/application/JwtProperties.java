package com.housekey.auth.application;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "housekey.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("housekey.jwt.secret must be configured.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("housekey.jwt.secret must be at least 32 bytes.");
        }
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofHours(1);
        }
        if (accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalStateException("housekey.jwt.access-token-ttl must be positive.");
        }
    }
}
