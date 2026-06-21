package com.housekey.auth.application;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.housekey.auth.api.AuthResponse;
import com.housekey.auth.api.LoginRequest;
import com.housekey.auth.api.RegisterRequest;
import com.housekey.auth.api.UserSummaryResponse;
import com.housekey.auth.domain.InvalidCredentialsException;
import com.housekey.users.domain.DuplicateUserException;
import com.housekey.users.domain.UserRole;
import com.housekey.users.infrastructure.UserEntity;
import com.housekey.users.infrastructure.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());
        validateUnique(username, email);

        UserEntity user = new UserEntity(
                username,
                email,
                passwordEncoder.encode(request.password()),
                UserRole.AGENCY,
                trimToNull(request.firstName()),
                trimToNull(request.lastName()),
                trimToNull(request.phone()),
                true,
                false);

        try {
            return toAuthResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateUserException("error.user.duplicate", Map.of(
                    "username", "error.user.duplicate.username",
                    "email", "error.user.duplicate.email"));
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String identity = request.usernameOrEmail().trim();
        UserEntity user = userRepository.findByUsernameIgnoreCase(identity)
                .or(() -> userRepository.findByEmailIgnoreCase(identity))
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isEnabled() || user.isLocked() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    private void validateUnique(String username, String email) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            fieldErrors.put("username", "error.user.duplicate.username");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            fieldErrors.put("email", "error.user.duplicate.email");
        }
        if (!fieldErrors.isEmpty()) {
            throw new DuplicateUserException("error.user.duplicate", fieldErrors);
        }
    }

    private AuthResponse toAuthResponse(UserEntity user) {
        JwtService.IssuedToken token = jwtService.issueToken(user);
        return new AuthResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                UserSummaryResponse.from(user));
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
