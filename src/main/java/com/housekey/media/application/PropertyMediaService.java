package com.housekey.media.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.listings.infrastructure.PropertyListingEntity;
import com.housekey.listings.infrastructure.PropertyListingRepository;
import com.housekey.media.api.MediaDtos.MediaOrderItemRequest;
import com.housekey.media.api.MediaDtos.MediaOrderUpdateRequest;
import com.housekey.media.api.MediaDtos.PropertyMediaResponse;
import com.housekey.media.domain.MediaKind;
import com.housekey.media.domain.MediaValidationException;
import com.housekey.media.infrastructure.MediaAssetEntity;
import com.housekey.media.infrastructure.PropertyMediaEntity;
import com.housekey.media.infrastructure.PropertyMediaRepository;
import com.housekey.media.mapper.PropertyMediaMapper;
import com.housekey.shared.error.LocalizedAccessDeniedException;
import com.housekey.shared.error.ResourceNotFoundException;
import com.housekey.users.domain.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PropertyMediaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyMediaService.class);

    private final PropertyListingRepository propertyRepository;
    private final PropertyMediaRepository mediaRepository;
    private final ImageProcessingService imageProcessingService;
    private final FileStorageService fileStorageService;
    private final PropertyMediaMapper mapper;

    public PropertyMediaService(
            PropertyListingRepository propertyRepository,
            PropertyMediaRepository mediaRepository,
            ImageProcessingService imageProcessingService,
            FileStorageService fileStorageService,
            PropertyMediaMapper mapper) {
        this.propertyRepository = propertyRepository;
        this.mediaRepository = mediaRepository;
        this.imageProcessingService = imageProcessingService;
        this.fileStorageService = fileStorageService;
        this.mapper = mapper;
    }

    public List<PropertyMediaResponse> uploadGalleryImages(
            Long propertyId,
            List<MultipartFile> files,
            AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);
        validateFilesPresent(files);

        int sortOrder = mediaRepository.findMaxSortOrder(propertyId, MediaKind.IMAGE) + 1;
        List<String> writtenKeys = new ArrayList<>();
        registerRollbackCleanup(writtenKeys);

        try {
            List<PropertyMediaEntity> entities = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                entities.add(createMediaEntity(
                        propertyId,
                        MediaKind.IMAGE,
                        sortOrder++,
                        files.get(i),
                        "files[" + i + "]",
                        principal.id(),
                        writtenKeys));
            }

            List<PropertyMediaEntity> saved = mediaRepository.saveAll(entities);
            mediaRepository.flush();
            return saved.stream()
                    .sorted(Comparator.comparing(PropertyMediaEntity::getSortOrder).thenComparing(PropertyMediaEntity::getId))
                    .map(mapper::toResponse)
                    .toList();
        } catch (IOException ex) {
            cleanupWrittenFiles(writtenKeys);
            throw new IllegalStateException("Image file could not be stored.", ex);
        } catch (RuntimeException ex) {
            cleanupWrittenFiles(writtenKeys);
            throw ex;
        }
    }

    public PropertyMediaResponse uploadFloorPlanImage(
            Long propertyId,
            Integer planId,
            MultipartFile file,
            AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);
        if (planId == null || planId < 1 || planId > property.getPlans().size()) {
            throw new ResourceNotFoundException("error.media.floorPlanNotFound", planId);
        }

        List<String> writtenKeys = new ArrayList<>();
        registerRollbackCleanup(writtenKeys);
        try {
            PropertyMediaEntity entity = createMediaEntity(
                    propertyId,
                    MediaKind.FLOOR_PLAN,
                    planId - 1,
                    file,
                    "file",
                    principal.id(),
                    writtenKeys);
            PropertyMediaEntity saved = mediaRepository.saveAndFlush(entity);
            return mapper.toResponse(saved);
        } catch (IOException ex) {
            cleanupWrittenFiles(writtenKeys);
            throw new IllegalStateException("Image file could not be stored.", ex);
        } catch (RuntimeException ex) {
            cleanupWrittenFiles(writtenKeys);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<PropertyMediaResponse> getPropertyMedia(Long propertyId, AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);
        return mediaRepository.findByPropertyIdAndDeletedAtIsNullOrderBySortOrderAscIdAsc(propertyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<PropertyMediaResponse> updateGalleryOrder(
            Long propertyId,
            MediaOrderUpdateRequest request,
            AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);

        List<PropertyMediaEntity> media = mediaRepository
                .findByPropertyIdAndMediaKindAndDeletedAtIsNullOrderBySortOrderAscIdAsc(propertyId, MediaKind.IMAGE);
        Map<Long, PropertyMediaEntity> mediaById = new LinkedHashMap<>();
        for (PropertyMediaEntity item : media) {
            mediaById.put(item.getId(), item);
        }

        if (request != null && request.mediaIds() != null && !request.mediaIds().isEmpty()) {
            for (int i = 0; i < request.mediaIds().size(); i++) {
                PropertyMediaEntity item = mediaById.get(request.mediaIds().get(i));
                if (item == null) {
                    throw new ResourceNotFoundException("error.media.notFound", request.mediaIds().get(i));
                }
                item.updateSortOrder(i);
            }
        } else if (request != null && request.items() != null && !request.items().isEmpty()) {
            for (MediaOrderItemRequest itemRequest : request.items()) {
                if (itemRequest.id() == null || itemRequest.sortOrder() == null || itemRequest.sortOrder() < 0) {
                    throw new MediaValidationException(
                            "error.media.validation",
                            Map.of("items", "validation.media.orderItemRequired"));
                }
                PropertyMediaEntity item = mediaById.get(itemRequest.id());
                if (item == null) {
                    throw new ResourceNotFoundException("error.media.notFound", itemRequest.id());
                }
                item.updateSortOrder(itemRequest.sortOrder());
            }
        } else {
            throw new MediaValidationException(
                    "error.media.validation",
                    Map.of("mediaIds", "validation.media.idRequired"));
        }

        return mediaRepository.saveAll(media)
                .stream()
                .sorted(Comparator.comparing(PropertyMediaEntity::getSortOrder).thenComparing(PropertyMediaEntity::getId))
                .map(mapper::toResponse)
                .toList();
    }

    public PropertyMediaResponse updateMetadata(
            Long propertyId,
            Long mediaId,
            Map<String, Object> patch,
            AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);
        PropertyMediaEntity media = findActiveMedia(propertyId, mediaId);

        if (patch != null && patch.containsKey("altText")) {
            media.updateAltText(trimToNull(Objects.toString(patch.get("altText"), null)));
        }
        if (patch != null && patch.containsKey("sortOrder")) {
            media.updateSortOrder(nonNegativeInteger(patch.get("sortOrder"), "sortOrder"));
        }

        return mapper.toResponse(mediaRepository.save(media));
    }

    public void deleteMedia(Long propertyId, Long mediaId, AuthenticatedUser principal) {
        PropertyListingEntity property = findProperty(propertyId);
        assertCanManage(property, principal);
        PropertyMediaEntity media = findActiveMedia(propertyId, mediaId);
        media.softDelete(LocalDateTime.now());
        mediaRepository.save(media);
    }

    private PropertyMediaEntity createMediaEntity(
            Long propertyId,
            MediaKind mediaKind,
            int sortOrder,
            MultipartFile file,
            String fieldPath,
            Long uploadedByUserId,
            List<String> writtenKeys) throws IOException {
        ProcessedImage processedImage = imageProcessingService.process(file, fieldPath);
        String mediaToken = UUID.randomUUID().toString();
        PropertyMediaEntity entity = new PropertyMediaEntity(propertyId, mediaKind, sortOrder, uploadedByUserId);

        for (ProcessedImageVariant variant : processedImage.variants()) {
            String storageKey = storageKey(propertyId, mediaToken, variant);
            StoredFile storedFile = fileStorageService.store(
                    storageKey,
                    new ByteArrayInputStream(variant.bytes()),
                    variant.bytes().length,
                    variant.contentType());
            writtenKeys.add(storedFile.storageKey());
            entity.addAsset(new MediaAssetEntity(
                    propertyId,
                    storedFile.storageKey(),
                    storedFile.relativeUrl(),
                    processedImage.originalFilename(),
                    storedFile.contentType(),
                    storedFile.byteSize(),
                    variant.width(),
                    variant.height(),
                    variant.variantType(),
                    mediaKind,
                    uploadedByUserId));
        }
        return entity;
    }

    private String storageKey(Long propertyId, String mediaToken, ProcessedImageVariant variant) {
        return "properties/%d/media/%s/%s.%s".formatted(
                propertyId,
                mediaToken,
                variant.variantType().name().toLowerCase(java.util.Locale.ROOT),
                variant.extension());
    }

    private PropertyListingEntity findProperty(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", propertyId));
    }

    private PropertyMediaEntity findActiveMedia(Long propertyId, Long mediaId) {
        return mediaRepository.findByIdAndPropertyIdAndDeletedAtIsNull(mediaId, propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.media.notFound", mediaId));
    }

    private void assertCanManage(PropertyListingEntity property, AuthenticatedUser principal) {
        if (principal == null) {
            throw new LocalizedAccessDeniedException("error.auth.required");
        }
        if (principal.role() == UserRole.ADMIN) {
            return;
        }
        if (Objects.equals(property.getOwnerUserId(), principal.id())) {
            return;
        }
        throw new LocalizedAccessDeniedException("error.accessDenied.manageListing");
    }

    private void validateFilesPresent(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new MediaValidationException(
                    "error.media.validation",
                    Map.of("files", "validation.media.filesRequired"));
        }
    }

    private Integer nonNegativeInteger(Object value, String fieldName) {
        if (value == null) {
            throw new MediaValidationException(
                    "error.media.validation",
                    Map.of(fieldName, "validation.value.required"));
        }
        try {
            int parsed = value instanceof Number number
                    ? number.intValue()
                    : Integer.parseInt(value.toString());
            if (parsed < 0) {
                throw new NumberFormatException("negative");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new MediaValidationException(
                    "error.media.validation",
                    Map.of(fieldName, "validation.value.nonNegativeWholeNumber"));
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void registerRollbackCleanup(List<String> writtenKeys) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    cleanupWrittenFiles(writtenKeys);
                }
            }
        });
    }

    private void cleanupWrittenFiles(List<String> storageKeys) {
        for (String storageKey : storageKeys) {
            try {
                fileStorageService.delete(storageKey);
            } catch (IOException ex) {
                LOGGER.warn("Failed to clean up stored media file {}", storageKey, ex);
            }
        }
    }
}
