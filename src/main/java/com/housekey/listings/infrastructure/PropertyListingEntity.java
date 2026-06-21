package com.housekey.listings.infrastructure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.housekey.listings.domain.ListingLifecycleStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "property_listing")
public class PropertyListingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "property_listing_id_generator")
    @SequenceGenerator(name = "property_listing_id_generator", sequenceName = "property_listing_id_seq", allocationSize = 1)
    private Long id;

    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "property_type")
    private String propertyType;

    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "latitude")),
            @AttributeOverride(name = "lng", column = @Column(name = "longitude"))
    })
    private GeoLocationValue location;

    @Column(name = "formatted_address")
    private String formattedAddress;

    private boolean featured;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "sale", column = @Column(name = "price_dollar_sale")),
            @AttributeOverride(name = "rent", column = @Column(name = "price_dollar_rent"))
    })
    private PriceValue priceDollar;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "sale", column = @Column(name = "price_euro_sale")),
            @AttributeOverride(name = "rent", column = @Column(name = "price_euro_rent"))
    })
    private PriceValue priceEuro;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "sale", column = @Column(name = "price_dinars_sale")),
            @AttributeOverride(name = "rent", column = @Column(name = "price_dinars_rent"))
    })
    private PriceValue priceDinars;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer garages;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "area_value")),
            @AttributeOverride(name = "unit", column = @Column(name = "area_unit"))
    })
    private AreaValue area;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "ratings_count")
    private Integer ratingsCount;

    @Column(name = "ratings_value")
    private Integer ratingsValue;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "last_update_at")
    private LocalDateTime lastUpdateAt;

    private Integer views;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "agent_user_id")
    private Long agentUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status")
    private ListingLifecycleStatus lifecycleStatus = ListingLifecycleStatus.DRAFT;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_status", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "name")
    private List<String> propertyStatus = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_neighborhood", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "name")
    private List<String> neighborhood = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_street", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "name")
    private List<String> street = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_feature", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "name")
    private List<String> features = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_additional_feature", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    private List<AdditionalFeatureValue> additionalFeatures = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_gallery_image", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    private List<GalleryImageValue> gallery = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_floor_plan", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    private List<FloorPlanValue> plans = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "listing_video", joinColumns = @JoinColumn(name = "property_listing_id"))
    @OrderColumn(name = "sort_order")
    private List<VideoValue> videos = new ArrayList<>();

    protected PropertyListingEntity() {
    }

    public PropertyListingEntity(Long ownerUserId, Long agentUserId) {
        this.ownerUserId = ownerUserId;
        this.agentUserId = agentUserId;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (lastUpdateAt == null) {
            lastUpdateAt = now;
        }
        if (lifecycleStatus == null) {
            lifecycleStatus = ListingLifecycleStatus.DRAFT;
        }
        if (lifecycleStatus == ListingLifecycleStatus.PUBLISHED && publishedAt == null) {
            publishedAt = now;
        }
        if (ratingsCount == null) {
            ratingsCount = 0;
        }
        if (ratingsValue == null) {
            ratingsValue = 0;
        }
        if (views == null) {
            views = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        LocalDateTime now = LocalDateTime.now();
        updatedAt = now;
        lastUpdateAt = now;
        if (lifecycleStatus == ListingLifecycleStatus.PUBLISHED && publishedAt == null) {
            publishedAt = now;
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public String getCity() {
        return city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public GeoLocationValue getLocation() {
        return location;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public boolean isFeatured() {
        return featured;
    }

    public PriceValue getPriceDollar() {
        return priceDollar;
    }

    public PriceValue getPriceEuro() {
        return priceEuro;
    }

    public PriceValue getPriceDinars() {
        return priceDinars;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public Integer getGarages() {
        return garages;
    }

    public AreaValue getArea() {
        return area;
    }

    public Integer getYearBuilt() {
        return yearBuilt;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public Integer getRatingsValue() {
        return ratingsValue;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public LocalDateTime getLastUpdateAt() {
        return lastUpdateAt;
    }

    public Integer getViews() {
        return views;
    }

    public Long getAgentId() {
        return agentId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public Long getAgentUserId() {
        return agentUserId;
    }

    public ListingLifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<String> getPropertyStatus() {
        return propertyStatus;
    }

    public List<String> getNeighborhood() {
        return neighborhood;
    }

    public List<String> getStreet() {
        return street;
    }

    public List<String> getFeatures() {
        return features;
    }

    public List<AdditionalFeatureValue> getAdditionalFeatures() {
        return additionalFeatures;
    }

    public List<GalleryImageValue> getGallery() {
        return gallery;
    }

    public List<FloorPlanValue> getPlans() {
        return plans;
    }

    public List<VideoValue> getVideos() {
        return videos;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setLocation(GeoLocationValue location) {
        this.location = location;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public void setPriceDollar(PriceValue priceDollar) {
        this.priceDollar = priceDollar;
    }

    public void setPriceEuro(PriceValue priceEuro) {
        this.priceEuro = priceEuro;
    }

    public void setPriceDinars(PriceValue priceDinars) {
        this.priceDinars = priceDinars;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public void setGarages(Integer garages) {
        this.garages = garages;
    }

    public void setArea(AreaValue area) {
        this.area = area;
    }

    public void setYearBuilt(Integer yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public void setRatingsValue(Integer ratingsValue) {
        this.ratingsValue = ratingsValue;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setLastUpdateAt(LocalDateTime lastUpdateAt) {
        this.lastUpdateAt = lastUpdateAt;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public void setLifecycleStatus(ListingLifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    @Embeddable
    public static class GeoLocationValue {
        private BigDecimal lat;
        private BigDecimal lng;

        protected GeoLocationValue() {
        }

        public GeoLocationValue(BigDecimal lat, BigDecimal lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public BigDecimal getLat() {
            return lat;
        }

        public BigDecimal getLng() {
            return lng;
        }
    }

    @Embeddable
    public static class PriceValue {
        private Long sale;
        private Long rent;

        protected PriceValue() {
        }

        public PriceValue(Long sale, Long rent) {
            this.sale = sale;
            this.rent = rent;
        }

        public Long getSale() {
            return sale;
        }

        public Long getRent() {
            return rent;
        }
    }

    @Embeddable
    public static class AreaValue {
        @Column(name = "area_value")
        private Integer value;

        @Column(name = "area_unit")
        private String unit;

        protected AreaValue() {
        }

        public AreaValue(Integer value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        public Integer getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }
    }

    @Embeddable
    public static class AdditionalFeatureValue {
        private String name;

        @Column(name = "feature_value")
        private String value;

        protected AdditionalFeatureValue() {
        }

        public AdditionalFeatureValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    @Embeddable
    public static class GalleryImageValue {
        @Column(name = "small_url")
        private String small;

        @Column(name = "medium_url")
        private String medium;

        @Column(name = "big_url")
        private String big;

        protected GalleryImageValue() {
        }

        public GalleryImageValue(String small, String medium, String big) {
            this.small = small;
            this.medium = medium;
            this.big = big;
        }

        public String getSmall() {
            return small;
        }

        public String getMedium() {
            return medium;
        }

        public String getBig() {
            return big;
        }
    }

    @Embeddable
    public static class FloorPlanValue {
        private String name;

        @Column(name = "description")
        private String desc;

        @Embedded
        @AttributeOverrides({
                @AttributeOverride(name = "value", column = @Column(name = "area_value")),
                @AttributeOverride(name = "unit", column = @Column(name = "area_unit"))
        })
        private AreaValue area;

        private Integer rooms;

        private Integer baths;

        private String image;

        protected FloorPlanValue() {
        }

        public FloorPlanValue(String name, String desc, AreaValue area, Integer rooms, Integer baths, String image) {
            this.name = name;
            this.desc = desc;
            this.area = area;
            this.rooms = rooms;
            this.baths = baths;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public AreaValue getArea() {
            return area;
        }

        public Integer getRooms() {
            return rooms;
        }

        public Integer getBaths() {
            return baths;
        }

        public String getImage() {
            return image;
        }
    }

    @Embeddable
    public static class VideoValue {
        private String name;

        private String link;

        protected VideoValue() {
        }

        public VideoValue(String name, String link) {
            this.name = name;
            this.link = link;
        }

        public String getName() {
            return name;
        }

        public String getLink() {
            return link;
        }
    }
}
