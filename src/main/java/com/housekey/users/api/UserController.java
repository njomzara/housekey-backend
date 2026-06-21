package com.housekey.users.api;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.users.application.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return currentUserService.getCurrentUser(principal);
    }
}
