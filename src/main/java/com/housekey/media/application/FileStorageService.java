package com.housekey.media.application;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    StoredFile store(String storageKey, InputStream content, long contentLength, String contentType) throws IOException;

    void delete(String storageKey) throws IOException;
}
