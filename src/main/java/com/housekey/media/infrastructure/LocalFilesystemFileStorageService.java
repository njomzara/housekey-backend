package com.housekey.media.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.housekey.media.application.FileStorageService;
import com.housekey.media.application.MediaProperties;
import com.housekey.media.application.StoredFile;
import com.housekey.shared.error.LocalizedIllegalArgumentException;
import org.springframework.stereotype.Service;

@Service
public class LocalFilesystemFileStorageService implements FileStorageService {

    private final Path root;
    private final String publicUrlPrefix;

    public LocalFilesystemFileStorageService(MediaProperties properties) throws IOException {
        this.root = properties.storage().root().toAbsolutePath().normalize();
        this.publicUrlPrefix = properties.storage().publicUrlPrefix();
        Files.createDirectories(root);
    }

    @Override
    public StoredFile store(String storageKey, InputStream content, long contentLength, String contentType) throws IOException {
        Path target = resolve(storageKey);
        Files.createDirectories(target.getParent());

        Path temporary = Files.createTempFile(target.getParent(), ".upload-", ".tmp");
        try {
            Files.copy(content, temporary, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }

        return new StoredFile(
                storageKey,
                publicUrlPrefix + "/" + storageKey,
                contentType,
                Files.size(target));
    }

    @Override
    public void delete(String storageKey) throws IOException {
        Files.deleteIfExists(resolve(storageKey));
    }

    private Path resolve(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new LocalizedIllegalArgumentException("validation.storage.key.required");
        }

        String normalizedKey = storageKey.replace('\\', '/');
        if (normalizedKey.startsWith("/") || normalizedKey.contains("../") || normalizedKey.contains("..\\")) {
            throw new LocalizedIllegalArgumentException("validation.storage.key.invalid");
        }

        Path target = root.resolve(normalizedKey).normalize();
        if (!target.startsWith(root)) {
            throw new LocalizedIllegalArgumentException("validation.storage.key.escapesRoot");
        }
        return target;
    }
}
