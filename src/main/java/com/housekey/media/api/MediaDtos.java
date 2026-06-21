package com.housekey.media.api;

import java.util.List;

public final class MediaDtos {

    private MediaDtos() {
    }

    public record PropertyMediaResponse(
            Long id,
            Long propertyId,
            String mediaKind,
            Integer sortOrder,
            String altText,
            Long uploadedByUserId,
            String createdAt,
            String small,
            String medium,
            String big,
            String original,
            List<MediaAssetResponse> variants) {
    }

    public record MediaAssetResponse(
            Long id,
            String variantType,
            String url,
            String storageKey,
            String originalFilename,
            String contentType,
            Long byteSize,
            Integer width,
            Integer height) {
    }

    public record MediaOrderUpdateRequest(
            List<Long> mediaIds,
            List<MediaOrderItemRequest> items) {
    }

    public record MediaOrderItemRequest(
            Long id,
            Integer sortOrder) {
    }
}
