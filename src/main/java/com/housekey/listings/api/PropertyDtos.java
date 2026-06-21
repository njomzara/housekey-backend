package com.housekey.listings.api;

import java.math.BigDecimal;
import java.util.List;

public final class PropertyDtos {

    private PropertyDtos() {
    }

    public record PropertyResponse(
            Long id,
            String title,
            String desc,
            String propertyType,
            List<String> propertyStatus,
            String city,
            String zipCode,
            List<String> neighborhood,
            List<String> street,
            LocationResponse location,
            String formattedAddress,
            List<String> features,
            boolean featured,
            PriceResponse priceDollar,
            PriceResponse priceEuro,
            PriceResponse priceDinars,
            Integer bedrooms,
            Integer bathrooms,
            Integer garages,
            AreaResponse area,
            Integer yearBuilt,
            Integer ratingsCount,
            Integer ratingsValue,
            List<AdditionalFeatureResponse> additionalFeatures,
            List<GalleryResponse> gallery,
            List<PlanResponse> plans,
            List<VideoResponse> videos,
            String published,
            String lastUpdate,
            Integer views,
            Long agentId) {
    }

    public record LocationResponse(
            Long propertyId,
            BigDecimal lat,
            BigDecimal lng) {
    }

    public record PriceResponse(
            Long sale,
            Long rent) {
    }

    public record AreaResponse(
            Integer value,
            String unit) {
    }

    public record AdditionalFeatureResponse(
            int id,
            String name,
            String value) {
    }

    public record GalleryResponse(
            int id,
            String small,
            String medium,
            String big,
            Long mediaId,
            Integer sortOrder,
            String altText) {

        public GalleryResponse(int id, String small, String medium, String big) {
            this(id, small, medium, big, null, null, null);
        }
    }

    public record PlanResponse(
            int id,
            String name,
            String desc,
            AreaResponse area,
            Integer rooms,
            Integer baths,
            String image) {
    }

    public record VideoResponse(
            int id,
            String name,
            String link) {
    }
}
