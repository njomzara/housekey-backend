package com.housekey.catalog.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record CatalogItemResponse(
            int id,
            String code,
            String name) {

        public CatalogItemResponse(int id, String name) {
            this(id, codeFrom(name), name);
        }
    }

    public record FeatureResponse(
            int id,
            String code,
            String name,
            boolean selected) {

        public FeatureResponse(int id, String name, boolean selected) {
            this(id, codeFrom(name), name, selected);
        }
    }

    public record LocationsResponse(
            List<CatalogItemResponse> cities,
            List<CatalogItemResponse> zipCodes,
            List<NeighborhoodResponse> neighborhoods,
            List<StreetResponse> streets,
            List<PropertyLocationResponse> propertyLocations) {
    }

    public record NeighborhoodResponse(
            int id,
            String code,
            String name,
            int cityId) {

        public NeighborhoodResponse(int id, String name, int cityId) {
            this(id, codeFrom(name), name, cityId);
        }
    }

    public record StreetResponse(
            int id,
            String code,
            String name,
            int cityId,
            int neighborhoodId) {

        public StreetResponse(int id, String name, int cityId, int neighborhoodId) {
            this(id, codeFrom(name), name, cityId, neighborhoodId);
        }
    }

    public record PropertyLocationResponse(
            Long propertyId,
            BigDecimal lat,
            BigDecimal lng) {
    }

    private static String codeFrom(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
