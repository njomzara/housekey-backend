package com.housekey.listings.api;

import java.util.List;

import com.housekey.listings.api.PropertyDtos.GalleryResponse;

public record MyPropertyResponse(
        Long id,
        String title,
        String desc,
        String propertyType,
        List<String> propertyStatus,
        String city,
        String formattedAddress,
        boolean featured,
        List<GalleryResponse> gallery,
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
