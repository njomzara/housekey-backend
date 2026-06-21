package com.housekey.media.infrastructure;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.housekey.media.domain.MediaKind;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "property_media")
public class PropertyMediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id")
    private Long propertyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_kind")
    private MediaKind mediaKind;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "uploaded_by_user_id")
    private Long uploadedByUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "propertyMedia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<MediaAssetEntity> assets = new ArrayList<>();

    protected PropertyMediaEntity() {
    }

    public PropertyMediaEntity(Long propertyId, MediaKind mediaKind, Integer sortOrder, Long uploadedByUserId) {
        this.propertyId = propertyId;
        this.mediaKind = mediaKind;
        this.sortOrder = sortOrder;
        this.uploadedByUserId = uploadedByUserId;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public MediaKind getMediaKind() {
        return mediaKind;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getAltText() {
        return altText;
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

    public List<MediaAssetEntity> getAssets() {
        return assets;
    }

    public void addAsset(MediaAssetEntity asset) {
        assets.add(asset);
        asset.setPropertyMedia(this);
    }

    public void updateAltText(String altText) {
        this.altText = altText;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void softDelete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
        for (MediaAssetEntity asset : assets) {
            asset.softDelete(deletedAt);
        }
    }
}
