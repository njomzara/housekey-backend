package com.housekey.media.application;

import java.util.List;

public record ProcessedImage(
        String originalFilename,
        List<ProcessedImageVariant> variants) {
}
