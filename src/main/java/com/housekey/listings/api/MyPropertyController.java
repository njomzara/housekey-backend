package com.housekey.listings.api;

import java.util.List;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.listings.application.PropertyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/properties")
public class MyPropertyController {

    private final PropertyManagementService propertyManagementService;

    public MyPropertyController(PropertyManagementService propertyManagementService) {
        this.propertyManagementService = propertyManagementService;
    }

    @GetMapping
    @Operation(summary = "Get property listings owned by the current user")
    public List<MyPropertyResponse> mine(@AuthenticationPrincipal AuthenticatedUser principal) {
        return propertyManagementService.getMyProperties(principal);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current user's full property listing detail")
    public PropertyDetailResponse getMine(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return propertyManagementService.getMyProperty(id, principal);
    }
}
