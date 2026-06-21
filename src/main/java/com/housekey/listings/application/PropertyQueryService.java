package com.housekey.listings.application;

import java.util.List;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.catalog.application.CatalogLocalizationService;
import com.housekey.listings.api.PropertyDtos.PropertyResponse;
import com.housekey.listings.domain.ListingLifecycleStatus;
import com.housekey.listings.domain.PropertySearchCriteria;
import com.housekey.listings.infrastructure.PropertyListingEntity;
import com.housekey.listings.infrastructure.PropertyListingRepository;
import com.housekey.listings.infrastructure.PropertyListingSpecifications;
import com.housekey.listings.mapper.PropertyListingMapper;
import com.housekey.media.application.PropertyMediaQueryService;
import com.housekey.shared.error.ResourceNotFoundException;
import com.housekey.shared.web.PageResponse;
import com.housekey.shared.web.PaginationResponse;
import com.housekey.users.domain.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PropertyQueryService {

    private static final int RELATED_LIMIT = 6;

    private final PropertyListingRepository repository;
    private final PropertyListingMapper mapper;
    private final PropertyMediaQueryService mediaQueryService;
    private final CatalogLocalizationService catalogLocalizationService;

    public PropertyQueryService(
            PropertyListingRepository repository,
            PropertyListingMapper mapper,
            PropertyMediaQueryService mediaQueryService,
            CatalogLocalizationService catalogLocalizationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.mediaQueryService = mediaQueryService;
        this.catalogLocalizationService = catalogLocalizationService;
    }

    public PageResponse<PropertyResponse> search(PropertySearchCriteria criteria) {
        PropertySearchCriteria canonicalCriteria = catalogLocalizationService.canonicalize(criteria);
        Pageable pageable = PageRequest.of(canonicalCriteria.page() - 1, canonicalCriteria.size());
        Page<PropertyListingEntity> result = repository.findAll(
                PropertyListingSpecifications.matching(canonicalCriteria),
                pageable);

        List<PropertyListingEntity> listings = result.getContent();
        var galleriesByPropertyId = mediaQueryService.galleryResponsesByPropertyIds(
                listings.stream().map(PropertyListingEntity::getId).toList());
        List<PropertyResponse> data = listings.stream()
                .map(entity -> mapper.toResponse(entity, galleriesByPropertyId.get(entity.getId())))
                .toList();

        return new PageResponse<>(data, PaginationResponse.from(result, canonicalCriteria.page(), canonicalCriteria.size()));
    }

    public PropertyResponse getById(Long id, AuthenticatedUser principal) {
        PropertyListingEntity listing = repository.findById(id)
                .filter(entity -> canView(entity, principal))
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", id));
        return mapper.toResponse(listing, mediaQueryService.galleryResponsesOrNull(listing.getId()));
    }

    public List<PropertyResponse> getFeatured() {
        List<PropertyListingEntity> listings = repository
                .findByFeaturedTrueAndLifecycleStatusOrderByPublishedAtDesc(ListingLifecycleStatus.PUBLISHED);
        var galleriesByPropertyId = mediaQueryService.galleryResponsesByPropertyIds(
                listings.stream().map(PropertyListingEntity::getId).toList());
        return listings.stream()
                .map(entity -> mapper.toResponse(entity, galleriesByPropertyId.get(entity.getId())))
                .toList();
    }

    public List<PropertyResponse> getRelated(Long id) {
        PropertyListingEntity current = repository.findById(id)
                .filter(entity -> entity.getLifecycleStatus() == ListingLifecycleStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", id));

        List<PropertyListingEntity> listings = repository.findByCityIgnoreCaseAndPropertyTypeIgnoreCaseAndIdNotAndLifecycleStatus(
                        current.getCity(),
                        current.getPropertyType(),
                        current.getId(),
                        ListingLifecycleStatus.PUBLISHED,
                        PageRequest.of(0, RELATED_LIMIT));
        var galleriesByPropertyId = mediaQueryService.galleryResponsesByPropertyIds(
                listings.stream().map(PropertyListingEntity::getId).toList());
        return listings.stream()
                .map(entity -> mapper.toResponse(entity, galleriesByPropertyId.get(entity.getId())))
                .toList();
    }

    private boolean canView(PropertyListingEntity entity, AuthenticatedUser principal) {
        if (entity.getLifecycleStatus() == ListingLifecycleStatus.PUBLISHED) {
            return true;
        }
        if (principal == null) {
            return false;
        }
        return principal.role() == UserRole.ADMIN || principal.id().equals(entity.getOwnerUserId());
    }
}
