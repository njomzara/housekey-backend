package com.housekey.media.application;

public record StoredFile(
        String storageKey,
        String relativeUrl,
        String contentType,
        long byteSize) {
}
