package com.housekey.listings.domain;

import java.util.List;

public record PropertySearchCriteria(
        int page,
        int size,
        String sort,
        String propertyType,
        List<String> propertyStatus,
        String city,
        String zipCode,
        List<String> neighborhood,
        List<String> street,
        Long priceFrom,
        Long priceTo,
        CurrencyCode currency,
        Integer bedroomsFrom,
        Integer bedroomsTo,
        Integer bathroomsFrom,
        Integer bathroomsTo,
        Integer garagesFrom,
        Integer garagesTo,
        Integer areaFrom,
        Integer areaTo,
        Integer yearBuiltFrom,
        Integer yearBuiltTo,
        List<String> features,
        Long agentId) {
}
