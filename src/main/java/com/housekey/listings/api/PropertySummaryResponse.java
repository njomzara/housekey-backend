package com.housekey.listings.api;

import java.util.List;

import com.housekey.listings.api.PropertyDtos.AreaResponse;
import com.housekey.listings.api.PropertyDtos.GalleryResponse;
import com.housekey.listings.api.PropertyDtos.LocationResponse;
import com.housekey.listings.api.PropertyDtos.PriceResponse;

public record PropertySummaryResponse(
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
        boolean featured,
        PriceResponse priceDollar,
        PriceResponse priceEuro,
        PriceResponse priceDinars,
        Integer bedrooms,
        Integer bathrooms,
        Integer garages,
        AreaResponse area,
        List<GalleryResponse> gallery,
        String published,
        String lastUpdate,
        Integer views,
        Long agentId) {
}
