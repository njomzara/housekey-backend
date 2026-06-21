package com.housekey.listings.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

import com.housekey.catalog.application.CatalogLocalizationService;
import com.housekey.listings.api.PropertyDtos.AdditionalFeatureResponse;
import com.housekey.listings.api.PropertyDtos.AreaResponse;
import com.housekey.listings.api.PropertyDtos.GalleryResponse;
import com.housekey.listings.api.PropertyDtos.LocationResponse;
import com.housekey.listings.api.PropertyDtos.PlanResponse;
import com.housekey.listings.api.PropertyDtos.PriceResponse;
import com.housekey.listings.api.PropertyDtos.PropertyResponse;
import com.housekey.listings.api.PropertyDtos.VideoResponse;
import com.housekey.listings.api.MyPropertyResponse;
import com.housekey.listings.api.PropertyDetailResponse;
import com.housekey.listings.api.PropertySummaryResponse;
import com.housekey.listings.application.PropertyWriteModel;
import com.housekey.listings.infrastructure.PropertyListingEntity;
import com.housekey.listings.infrastructure.PropertyListingEntity.AdditionalFeatureValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.AreaValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.FloorPlanValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.GalleryImageValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.GeoLocationValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.PriceValue;
import com.housekey.listings.infrastructure.PropertyListingEntity.VideoValue;
import org.springframework.stereotype.Component;

@Component
public class PropertyListingMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CatalogLocalizationService catalogLocalizationService;

    public PropertyListingMapper(CatalogLocalizationService catalogLocalizationService) {
        this.catalogLocalizationService = catalogLocalizationService;
    }

    public void apply(PropertyListingEntity entity, PropertyWriteModel model) {
        entity.setTitle(model.title());
        entity.setDescription(model.description());
        entity.setPropertyType(model.propertyType());
        entity.setCity(model.city());
        entity.setZipCode(model.zipCode());
        entity.setLocation(new GeoLocationValue(model.latitude(), model.longitude()));
        entity.setFormattedAddress(model.formattedAddress());
        entity.setFeatured(model.featured());
        entity.setPriceDollar(toPriceValue(model.priceDollar()));
        entity.setPriceEuro(toPriceValue(model.priceEuro()));
        entity.setPriceDinars(toPriceValue(model.priceDinars()));
        entity.setBedrooms(model.bedrooms());
        entity.setBathrooms(model.bathrooms());
        entity.setGarages(model.garages());
        entity.setArea(new AreaValue(model.areaValue(), model.areaUnit()));
        entity.setYearBuilt(model.yearBuilt());
        entity.setLifecycleStatus(model.lifecycleStatus());

        replace(entity.getPropertyStatus(), model.propertyStatus());
        replace(entity.getNeighborhood(), model.neighborhood());
        replace(entity.getStreet(), model.street());
        replace(entity.getFeatures(), model.features());

        replaceAdditionalFeatures(entity, model);
        replaceGallery(entity, model);
        replacePlans(entity, model);
        replaceVideos(entity, model);
    }

    public PropertyResponse toResponse(PropertyListingEntity entity) {
        return toResponse(entity, null);
    }

    public PropertyResponse toResponse(PropertyListingEntity entity, List<GalleryResponse> galleryOverride) {
        return new PropertyResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                catalogLocalizationService.localizePropertyType(entity.getPropertyType()),
                catalogLocalizationService.localizePropertyStatuses(entity.getPropertyStatus()),
                entity.getCity(),
                entity.getZipCode(),
                List.copyOf(entity.getNeighborhood()),
                List.copyOf(entity.getStreet()),
                toLocationResponse(entity.getId(), entity.getLocation()),
                entity.getFormattedAddress(),
                catalogLocalizationService.localizeFeatures(entity.getFeatures()),
                entity.isFeatured(),
                toPriceResponse(entity.getPriceDollar()),
                toPriceResponse(entity.getPriceEuro()),
                toPriceResponse(entity.getPriceDinars()),
                entity.getBedrooms(),
                entity.getBathrooms(),
                entity.getGarages(),
                toAreaResponse(entity.getArea()),
                entity.getYearBuilt(),
                entity.getRatingsCount(),
                entity.getRatingsValue(),
                toAdditionalFeatureResponses(entity.getAdditionalFeatures()),
                galleryResponses(entity, galleryOverride),
                toPlanResponses(entity.getPlans()),
                toVideoResponses(entity.getVideos()),
                format(entity.getPublishedAt()),
                format(entity.getLastUpdateAt()),
                entity.getViews(),
                entity.getAgentId());
    }

    public PropertySummaryResponse toSummaryResponse(PropertyListingEntity entity) {
        return toSummaryResponse(entity, null);
    }

    public PropertySummaryResponse toSummaryResponse(PropertyListingEntity entity, List<GalleryResponse> galleryOverride) {
        return new PropertySummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                catalogLocalizationService.localizePropertyType(entity.getPropertyType()),
                catalogLocalizationService.localizePropertyStatuses(entity.getPropertyStatus()),
                entity.getCity(),
                entity.getZipCode(),
                List.copyOf(entity.getNeighborhood()),
                List.copyOf(entity.getStreet()),
                toLocationResponse(entity.getId(), entity.getLocation()),
                entity.getFormattedAddress(),
                entity.isFeatured(),
                toPriceResponse(entity.getPriceDollar()),
                toPriceResponse(entity.getPriceEuro()),
                toPriceResponse(entity.getPriceDinars()),
                entity.getBedrooms(),
                entity.getBathrooms(),
                entity.getGarages(),
                toAreaResponse(entity.getArea()),
                galleryResponses(entity, galleryOverride),
                format(entity.getPublishedAt()),
                format(entity.getLastUpdateAt()),
                entity.getViews(),
                entity.getAgentId());
    }

    public PropertyDetailResponse toDetailResponse(PropertyListingEntity entity) {
        return toDetailResponse(entity, null);
    }

    public PropertyDetailResponse toDetailResponse(PropertyListingEntity entity, List<GalleryResponse> galleryOverride) {
        return new PropertyDetailResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                catalogLocalizationService.localizePropertyType(entity.getPropertyType()),
                catalogLocalizationService.localizePropertyStatuses(entity.getPropertyStatus()),
                entity.getCity(),
                entity.getZipCode(),
                List.copyOf(entity.getNeighborhood()),
                List.copyOf(entity.getStreet()),
                toLocationResponse(entity.getId(), entity.getLocation()),
                entity.getFormattedAddress(),
                catalogLocalizationService.localizeFeatures(entity.getFeatures()),
                entity.isFeatured(),
                toPriceResponse(entity.getPriceDollar()),
                toPriceResponse(entity.getPriceEuro()),
                toPriceResponse(entity.getPriceDinars()),
                entity.getBedrooms(),
                entity.getBathrooms(),
                entity.getGarages(),
                toAreaResponse(entity.getArea()),
                entity.getYearBuilt(),
                entity.getRatingsCount(),
                entity.getRatingsValue(),
                toAdditionalFeatureResponses(entity.getAdditionalFeatures()),
                galleryResponses(entity, galleryOverride),
                toPlanResponses(entity.getPlans()),
                toVideoResponses(entity.getVideos()),
                format(entity.getPublishedAt()),
                format(entity.getLastUpdateAt()),
                entity.getViews(),
                entity.getAgentId(),
                entity.getLifecycleStatus().name(),
                entity.getOwnerUserId(),
                entity.getAgentUserId(),
                format(entity.getCreatedAt()),
                format(entity.getUpdatedAt()),
                entity.getVersion());
    }

    public MyPropertyResponse toMyPropertyResponse(PropertyListingEntity entity) {
        return toMyPropertyResponse(entity, null);
    }

    public MyPropertyResponse toMyPropertyResponse(PropertyListingEntity entity, List<GalleryResponse> galleryOverride) {
        return new MyPropertyResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                catalogLocalizationService.localizePropertyType(entity.getPropertyType()),
                catalogLocalizationService.localizePropertyStatuses(entity.getPropertyStatus()),
                entity.getCity(),
                entity.getFormattedAddress(),
                entity.isFeatured(),
                galleryResponses(entity, galleryOverride),
                format(entity.getPublishedAt()),
                format(entity.getLastUpdateAt()),
                entity.getViews(),
                entity.getAgentId(),
                entity.getLifecycleStatus().name(),
                entity.getOwnerUserId(),
                entity.getAgentUserId(),
                format(entity.getCreatedAt()),
                format(entity.getUpdatedAt()),
                entity.getVersion());
    }

    private LocationResponse toLocationResponse(Long propertyId, GeoLocationValue location) {
        if (location == null) {
            return new LocationResponse(propertyId, null, null);
        }
        return new LocationResponse(propertyId, location.getLat(), location.getLng());
    }

    private PriceResponse toPriceResponse(PriceValue price) {
        if (price == null) {
            return new PriceResponse(null, null);
        }
        return new PriceResponse(price.getSale(), price.getRent());
    }

    private PriceValue toPriceValue(PropertyWriteModel.PriceModel price) {
        return new PriceValue(price.sale(), price.rent());
    }

    private AreaResponse toAreaResponse(AreaValue area) {
        if (area == null) {
            return new AreaResponse(null, null);
        }
        return new AreaResponse(area.getValue(), area.getUnit());
    }

    private List<AdditionalFeatureResponse> toAdditionalFeatureResponses(List<AdditionalFeatureValue> values) {
        return IntStream.range(0, values.size())
                .mapToObj(index -> {
                    AdditionalFeatureValue value = values.get(index);
                    return new AdditionalFeatureResponse(index + 1, value.getName(), value.getValue());
                })
                .toList();
    }

    private List<GalleryResponse> toGalleryResponses(List<GalleryImageValue> values) {
        return IntStream.range(0, values.size())
                .mapToObj(index -> {
                    GalleryImageValue value = values.get(index);
                    return new GalleryResponse(index + 1, value.getSmall(), value.getMedium(), value.getBig());
                })
                .toList();
    }

    private List<GalleryResponse> galleryResponses(
            PropertyListingEntity entity,
            List<GalleryResponse> galleryOverride) {
        if (galleryOverride != null) {
            return galleryOverride;
        }
        return toGalleryResponses(entity.getGallery());
    }

    private List<PlanResponse> toPlanResponses(List<FloorPlanValue> values) {
        return IntStream.range(0, values.size())
                .mapToObj(index -> {
                    FloorPlanValue value = values.get(index);
                    return new PlanResponse(
                            index + 1,
                            value.getName(),
                            value.getDesc(),
                            toAreaResponse(value.getArea()),
                            value.getRooms(),
                            value.getBaths(),
                            value.getImage());
                })
                .toList();
    }

    private List<VideoResponse> toVideoResponses(List<VideoValue> values) {
        return IntStream.range(0, values.size())
                .mapToObj(index -> {
                    VideoValue value = values.get(index);
                    return new VideoResponse(index + 1, value.getName(), value.getLink());
                })
                .toList();
    }

    private String format(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private void replace(List<String> target, List<String> values) {
        target.clear();
        target.addAll(values);
    }

    private void replaceAdditionalFeatures(PropertyListingEntity entity, PropertyWriteModel model) {
        entity.getAdditionalFeatures().clear();
        entity.getAdditionalFeatures().addAll(model.additionalFeatures().stream()
                .map(value -> new AdditionalFeatureValue(value.name(), value.value()))
                .toList());
    }

    private void replaceGallery(PropertyListingEntity entity, PropertyWriteModel model) {
        entity.getGallery().clear();
        entity.getGallery().addAll(model.gallery().stream()
                .map(value -> new GalleryImageValue(value.small(), value.medium(), value.big()))
                .toList());
    }

    private void replacePlans(PropertyListingEntity entity, PropertyWriteModel model) {
        entity.getPlans().clear();
        entity.getPlans().addAll(model.plans().stream()
                .map(value -> new FloorPlanValue(
                        value.name(),
                        value.desc(),
                        new AreaValue(value.areaValue(), value.areaUnit()),
                        value.rooms(),
                        value.baths(),
                        value.image()))
                .toList());
    }

    private void replaceVideos(PropertyListingEntity entity, PropertyWriteModel model) {
        entity.getVideos().clear();
        entity.getVideos().addAll(model.videos().stream()
                .map(value -> new VideoValue(value.name(), value.link()))
                .toList());
    }
}
