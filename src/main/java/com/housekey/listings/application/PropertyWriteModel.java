package com.housekey.listings.application;

import java.math.BigDecimal;
import java.util.List;

import com.housekey.listings.domain.ListingLifecycleStatus;

public record PropertyWriteModel(
        String title,
        String description,
        String propertyType,
        List<String> propertyStatus,
        String city,
        String zipCode,
        List<String> neighborhood,
        List<String> street,
        BigDecimal latitude,
        BigDecimal longitude,
        String formattedAddress,
        List<String> features,
        boolean featured,
        PriceModel priceDollar,
        PriceModel priceEuro,
        PriceModel priceDinars,
        Integer bedrooms,
        Integer bathrooms,
        Integer garages,
        Integer areaValue,
        String areaUnit,
        Integer yearBuilt,
        List<AdditionalFeatureModel> additionalFeatures,
        List<GalleryImageModel> gallery,
        List<FloorPlanModel> plans,
        List<VideoModel> videos,
        ListingLifecycleStatus lifecycleStatus) {

    public record PriceModel(Long sale, Long rent) {
    }

    public record AdditionalFeatureModel(String name, String value) {
    }

    public record GalleryImageModel(String small, String medium, String big) {
    }

    public record FloorPlanModel(
            String name,
            String desc,
            Integer areaValue,
            String areaUnit,
            Integer rooms,
            Integer baths,
            String image) {
    }

    public record VideoModel(String name, String link) {
    }
}
