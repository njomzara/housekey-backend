package com.housekey.media.application;

import com.housekey.media.domain.MediaVariantType;

public record ProcessedImageVariant(
        MediaVariantType variantType,
        byte[] bytes,
        String contentType,
        String extension,
        int width,
        int height) {
}
