package com.housekey.media.application;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "housekey.media")
public record MediaProperties(
        DataSize maxFileSize,
        Storage storage) {

    public MediaProperties {
        if (maxFileSize == null) {
            maxFileSize = DataSize.ofMegabytes(10);
        }
        if (storage == null) {
            storage = new Storage(Path.of("./storage/uploads"), "/uploads");
        }
    }

    public record Storage(
            Path root,
            String publicUrlPrefix) {

        public Storage {
            if (root == null) {
                root = Path.of("./storage/uploads");
            }
            if (publicUrlPrefix == null || publicUrlPrefix.isBlank()) {
                publicUrlPrefix = "/uploads";
            }
            if (!publicUrlPrefix.startsWith("/")) {
                publicUrlPrefix = "/" + publicUrlPrefix;
            }
            if (publicUrlPrefix.endsWith("/")) {
                publicUrlPrefix = publicUrlPrefix.substring(0, publicUrlPrefix.length() - 1);
            }
        }
    }
}
