package com.housekey.users.application;

import java.util.Locale;

import com.housekey.users.infrastructure.UserEntity;
import com.housekey.users.infrastructure.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DevUserSeeder implements ApplicationRunner {

    private final DevUserProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUserSeeder(
            DevUserProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        String username = normalizeUsername(required(properties.username(), "housekey.dev-user.username"));
        String email = normalizeEmail(required(properties.email(), "housekey.dev-user.email"));
        String password = required(properties.password(), "housekey.dev-user.password");

        if (userRepository.existsByUsernameIgnoreCase(username) || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        userRepository.save(new UserEntity(
                username,
                email,
                passwordEncoder.encode(password),
                properties.role(),
                trimToNull(properties.firstName()),
                trimToNull(properties.lastName()),
                trimToNull(properties.phone()),
                true,
                false));
    }

    private String required(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must be configured when dev user seeding is enabled.");
        }
        return value;
    }

    private String normalizeUsername(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
