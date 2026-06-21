package com.housekey.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 160) String usernameOrEmail,
        @NotBlank @Size(max = 128) String password) {
}
