package com.housekey.media.infrastructure;

import java.time.LocalDateTime;

import com.housekey.media.domain.MediaKind;
import com.housekey.media.domain.MediaVariantType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_assets")
public class MediaAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_media_id")
    private PropertyMediaEntity propertyMedia;

    @Column(name = "property_id")
    private Long propertyId;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "relative_url")
    private String relativeUrl;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "byte_size")
    private Long byteSize;

    private Integer width;

    private Integer height;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type")
    private MediaVariantType variantType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_kind")
    private MediaKind mediaKind;

    @Column(name = "uploaded_by_user_id")
    private Long uploadedByUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected MediaAssetEntity() {
    }

    public MediaAssetEntity(
            Long propertyId,
            String storageKey,
            String relativeUrl,
            String originalFilename,
            String contentType,
            Long byteSize,
            Integer width,
            Integer height,
            MediaVariantType variantType,
            MediaKind mediaKind,
            Long uploadedByUserId) {
        this.propertyId = propertyId;
        this.storageKey = storageKey;
        this.relativeUrl = relativeUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.byteSize = byteSize;
        this.width = width;
        this.height = height;
        this.variantType = variantType;
        this.mediaKind = mediaKind;
        this.uploadedByUserId = uploadedByUserId;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    void setPropertyMedia(PropertyMediaEntity propertyMedia) {
        this.propertyMedia = propertyMedia;
    }

    public Long getId() {
        return id;
    }

    public PropertyMediaEntity getPropertyMedia() {
        return propertyMedia;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public MediaVariantType getVariantType() {
        return variantType;
    }

    public MediaKind getMediaKind() {
        return mediaKind;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void softDelete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
