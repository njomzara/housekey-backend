package com.housekey.listings.api;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public final class PropertyWriteSections {

    private PropertyWriteSections() {
    }

    public record BasicSection(
            String title,
            String description,
            String desc,
            JsonNode propertyType,
            List<JsonNode> propertyStatus,
            List<JsonNode> listingStatuses,
            List<JsonNode> tags,
            JsonNode priceDollar,
            JsonNode priceEuro,
            JsonNode priceDinars,
            JsonNode salePrice,
            JsonNode rentPrice,
            List<JsonNode> gallery) {
    }

    public record AddressSection(
            JsonNode formattedAddress,
            JsonNode location,
            JsonNode city,
            JsonNode zipCode,
            List<JsonNode> neighborhoods,
            List<JsonNode> neighborhood,
            List<JsonNode> streets,
            List<JsonNode> street,
            JsonNode lat,
            JsonNode lng) {
    }

    public record AdditionalSection(
            JsonNode bedrooms,
            JsonNode bathrooms,
            JsonNode garages,
            JsonNode area,
            JsonNode yearBuilt,
            List<JsonNode> features) {
    }

    public record MediaSection(
            List<JsonNode> gallery,
            List<JsonNode> videos,
            List<JsonNode> plans,
            List<JsonNode> floorPlans,
            List<JsonNode> additionalFeatures,
            Boolean featured) {
    }
}
