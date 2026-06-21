package com.housekey.listings.api;

import java.util.List;

import com.housekey.listings.api.PropertyDtos.AdditionalFeatureResponse;
import com.housekey.listings.api.PropertyDtos.AreaResponse;
import com.housekey.listings.api.PropertyDtos.GalleryResponse;
import com.housekey.listings.api.PropertyDtos.LocationResponse;
import com.housekey.listings.api.PropertyDtos.PlanResponse;
import com.housekey.listings.api.PropertyDtos.PriceResponse;
import com.housekey.listings.api.PropertyDtos.VideoResponse;

public record PropertyDetailResponse(
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
        Long agentId,
        String lifecycleStatus,
        Long ownerUserId,
        Long agentUserId,
        String createdAt,
        String updatedAt,
        Long version) {
}
