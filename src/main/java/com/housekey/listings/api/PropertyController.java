package com.housekey.listings.api;

import java.util.Arrays;
import java.util.List;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.listings.api.PropertyDtos.PropertyResponse;
import com.housekey.listings.application.PropertyManagementService;
import com.housekey.listings.application.PropertyQueryService;
import com.housekey.listings.domain.CurrencyCode;
import com.housekey.listings.domain.PropertySearchCriteria;
import com.housekey.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyQueryService propertyQueryService;
    private final PropertyManagementService propertyManagementService;

    public PropertyController(
            PropertyQueryService propertyQueryService,
            PropertyManagementService propertyManagementService) {
        this.propertyQueryService = propertyQueryService;
        this.propertyManagementService = propertyManagementService;
    }

    @GetMapping
    @Operation(summary = "Search property listings")
    public PageResponse<PropertyResponse> search(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) List<String> propertyStatus,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) List<String> neighborhood,
            @RequestParam(required = false) List<String> street,
            @RequestParam(required = false) Long priceFrom,
            @RequestParam(required = false) Long priceTo,
            @RequestParam(required = false, defaultValue = "USD") String currency,
            @RequestParam(required = false) Integer bedroomsFrom,
            @RequestParam(required = false) Integer bedroomsTo,
            @RequestParam(required = false) Integer bathroomsFrom,
            @RequestParam(required = false) Integer bathroomsTo,
            @RequestParam(required = false) Integer garagesFrom,
            @RequestParam(required = false) Integer garagesTo,
            @RequestParam(required = false) Integer areaFrom,
            @RequestParam(required = false) Integer areaTo,
            @RequestParam(required = false) Integer yearBuiltFrom,
            @RequestParam(required = false) Integer yearBuiltTo,
            @RequestParam(required = false) List<String> features,
            @RequestParam(required = false) Long agentId) {
        PropertySearchCriteria criteria = new PropertySearchCriteria(
                page,
                size,
                sort,
                propertyType,
                splitValues(propertyStatus),
                city,
                zipCode,
                splitValues(neighborhood),
                splitValues(street),
                priceFrom,
                priceTo,
                CurrencyCode.from(currency),
                bedroomsFrom,
                bedroomsTo,
                bathroomsFrom,
                bathroomsTo,
                garagesFrom,
                garagesTo,
                areaFrom,
                areaTo,
                yearBuiltFrom,
                yearBuiltTo,
                splitValues(features),
                agentId);

        return propertyQueryService.search(criteria);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full property detail")
    public PropertyResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return propertyQueryService.getById(id, principal);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a property listing")
    public PropertyDetailResponse create(
            @Valid @RequestBody PropertyCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return propertyManagementService.create(request, principal);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a property listing")
    public PropertyDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PropertyUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return propertyManagementService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive a property listing")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        propertyManagementService.archive(id, principal);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured property listings")
    public List<PropertyResponse> featured() {
        return propertyQueryService.getFeatured();
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related property listings")
    public List<PropertyResponse> related(@PathVariable Long id) {
        return propertyQueryService.getRelated(id);
    }

    private List<String> splitValues(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
