package com.housekey.catalog.api;

import java.util.List;

import com.housekey.catalog.api.CatalogDtos.CatalogItemResponse;
import com.housekey.catalog.api.CatalogDtos.FeatureResponse;
import com.housekey.catalog.api.CatalogDtos.LocationsResponse;
import com.housekey.catalog.application.CatalogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogQueryService catalogQueryService;

    public CatalogController(CatalogQueryService catalogQueryService) {
        this.catalogQueryService = catalogQueryService;
    }

    @GetMapping("/property-types")
    @Operation(summary = "Get property type catalog")
    public List<CatalogItemResponse> propertyTypes() {
        return catalogQueryService.propertyTypes();
    }

    @GetMapping("/property-statuses")
    @Operation(summary = "Get property status catalog")
    public List<CatalogItemResponse> propertyStatuses() {
        return catalogQueryService.propertyStatuses();
    }

    @GetMapping("/features")
    @Operation(summary = "Get feature catalog")
    public List<FeatureResponse> features() {
        return catalogQueryService.features();
    }

    @GetMapping("/locations")
    @Operation(summary = "Get location catalog")
    public LocationsResponse locations() {
        return catalogQueryService.locations();
    }
}
