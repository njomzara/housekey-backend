package com.housekey.media.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.housekey.listings.api.PropertyDtos.GalleryResponse;
import com.housekey.media.api.MediaDtos.MediaAssetResponse;
import com.housekey.media.api.MediaDtos.PropertyMediaResponse;
import com.housekey.media.domain.MediaVariantType;
import com.housekey.media.infrastructure.MediaAssetEntity;
import com.housekey.media.infrastructure.PropertyMediaEntity;
import org.springframework.stereotype.Component;

@Component
public class PropertyMediaMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PropertyMediaResponse toResponse(PropertyMediaEntity entity) {
        Map<MediaVariantType, MediaAssetEntity> assets = assetsByVariant(entity);
        return new PropertyMediaResponse(
                entity.getId(),
                entity.getPropertyId(),
                entity.getMediaKind().name(),
                entity.getSortOrder(),
                entity.getAltText(),
                entity.getUploadedByUserId(),
                format(entity.getCreatedAt()),
                url(assets, MediaVariantType.SMALL),
                url(assets, MediaVariantType.MEDIUM),
                url(assets, MediaVariantType.BIG),
                url(assets, MediaVariantType.ORIGINAL),
                entity.getAssets().stream()
                        .filter(asset -> asset.getDeletedAt() == null)
                        .sorted(Comparator.comparing(asset -> asset.getVariantType().ordinal()))
                        .map(this::toAssetResponse)
                        .toList());
    }

    public GalleryResponse toGalleryResponse(PropertyMediaEntity entity, int index) {
        Map<MediaVariantType, MediaAssetEntity> assets = assetsByVariant(entity);
        String original = url(assets, MediaVariantType.ORIGINAL);
        String big = firstNonBlank(url(assets, MediaVariantType.BIG), original);
        String medium = firstNonBlank(url(assets, MediaVariantType.MEDIUM), big);
        String small = firstNonBlank(url(assets, MediaVariantType.SMALL), medium);
        return new GalleryResponse(
                index + 1,
                small,
                medium,
                big,
                entity.getId(),
                entity.getSortOrder(),
                entity.getAltText());
    }

    private MediaAssetResponse toAssetResponse(MediaAssetEntity entity) {
        return new MediaAssetResponse(
                entity.getId(),
                entity.getVariantType().name(),
                entity.getRelativeUrl(),
                entity.getStorageKey(),
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getByteSize(),
                entity.getWidth(),
                entity.getHeight());
    }

    private Map<MediaVariantType, MediaAssetEntity> assetsByVariant(PropertyMediaEntity entity) {
        Map<MediaVariantType, MediaAssetEntity> result = new EnumMap<>(MediaVariantType.class);
        for (MediaAssetEntity asset : entity.getAssets()) {
            if (asset.getDeletedAt() == null) {
                result.put(asset.getVariantType(), asset);
            }
        }
        return result;
    }

    private String url(Map<MediaVariantType, MediaAssetEntity> assets, MediaVariantType variantType) {
        MediaAssetEntity asset = assets.get(variantType);
        return asset == null ? null : asset.getRelativeUrl();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String format(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(DATE_TIME_FORMATTER);
    }
}
