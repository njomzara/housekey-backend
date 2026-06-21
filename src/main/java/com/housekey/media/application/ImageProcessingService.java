package com.housekey.media.application;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import com.housekey.media.domain.MediaValidationException;
import com.housekey.media.domain.MediaVariantType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageProcessingService {

    private static final int SMALL_MAX_WIDTH = 480;
    private static final int MEDIUM_MAX_WIDTH = 960;
    private static final int BIG_MAX_WIDTH = 1600;

    private final MediaProperties properties;

    public ImageProcessingService(MediaProperties properties) {
        this.properties = properties;
    }

    public ProcessedImage process(MultipartFile file, String fieldPath) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        if (file == null || file.isEmpty()) {
            fieldErrors.put(fieldPath, "validation.image.required");
            throw new MediaValidationException("error.media.validation", fieldErrors);
        }

        long maxBytes = properties.maxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            fieldErrors.put(fieldPath, "validation.image.maxSize");
            throw new MediaValidationException("error.media.validation", fieldErrors);
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!isSupportedContentType(contentType)) {
            fieldErrors.put(fieldPath, "validation.image.unsupportedContentType");
            throw new MediaValidationException("error.media.validation", fieldErrors);
        }

        byte[] originalBytes;
        try {
            originalBytes = file.getBytes();
        } catch (IOException ex) {
            fieldErrors.put(fieldPath, "validation.image.readFailed");
            throw new MediaValidationException("error.media.validation", fieldErrors);
        }

        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(originalBytes));
        } catch (IOException ex) {
            image = null;
        }
        if (image == null) {
            fieldErrors.put(fieldPath, "validation.image.invalid");
            throw new MediaValidationException("error.media.validation", fieldErrors);
        }

        OutputFormat variantFormat = outputFormat(contentType);
        List<ProcessedImageVariant> variants = List.of(
                new ProcessedImageVariant(
                        MediaVariantType.ORIGINAL,
                        originalBytes,
                        contentType,
                        extension(contentType),
                        image.getWidth(),
                        image.getHeight()),
                variant(MediaVariantType.SMALL, image, SMALL_MAX_WIDTH, variantFormat),
                variant(MediaVariantType.MEDIUM, image, MEDIUM_MAX_WIDTH, variantFormat),
                variant(MediaVariantType.BIG, image, BIG_MAX_WIDTH, variantFormat));

        return new ProcessedImage(safeFilename(file.getOriginalFilename()), variants);
    }

    private ProcessedImageVariant variant(
            MediaVariantType variantType,
            BufferedImage source,
            int maxWidth,
            OutputFormat outputFormat) {
        BufferedImage resized = resize(source, maxWidth, outputFormat);
        byte[] bytes = write(resized, outputFormat);
        return new ProcessedImageVariant(
                variantType,
                bytes,
                outputFormat.contentType(),
                outputFormat.extension(),
                resized.getWidth(),
                resized.getHeight());
    }

    private BufferedImage resize(BufferedImage source, int maxWidth, OutputFormat outputFormat) {
        int targetWidth = Math.min(maxWidth, source.getWidth());
        int targetHeight = Math.max(1, Math.round(source.getHeight() * (targetWidth / (float) source.getWidth())));
        boolean alpha = source.getColorModel().hasAlpha() && outputFormat.supportsAlpha();
        BufferedImage target = new BufferedImage(
                targetWidth,
                targetHeight,
                alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (!alpha) {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, targetWidth, targetHeight);
            }
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private byte[] write(BufferedImage image, OutputFormat outputFormat) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            boolean written = ImageIO.write(image, outputFormat.formatName(), output);
            if (!written) {
                throw new IllegalStateException("No ImageIO writer is available for " + outputFormat.formatName() + ".");
            }
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Image variant could not be generated.", ex);
        }
    }

    private boolean isSupportedContentType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        return "image/jpg".equals(normalized) ? "image/jpeg" : normalized;
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };
    }

    private OutputFormat outputFormat(String contentType) {
        if ("image/png".equals(contentType)) {
            return new OutputFormat("png", "image/png", "png", true);
        }
        if ("image/webp".equals(contentType) && ImageIO.getImageWritersByFormatName("webp").hasNext()) {
            return new OutputFormat("webp", "image/webp", "webp", true);
        }
        if ("image/webp".equals(contentType)) {
            return new OutputFormat("png", "image/png", "png", true);
        }
        return new OutputFormat("jpg", "image/jpeg", "jpg", false);
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        String filename = originalFilename.replace('\\', '/');
        int slash = filename.lastIndexOf('/');
        if (slash >= 0) {
            filename = filename.substring(slash + 1);
        }
        filename = filename.replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (filename.isBlank()) {
            return null;
        }
        return filename.length() > 255 ? filename.substring(filename.length() - 255) : filename;
    }

    private record OutputFormat(
            String formatName,
            String contentType,
            String extension,
            boolean supportsAlpha) {
    }
}
