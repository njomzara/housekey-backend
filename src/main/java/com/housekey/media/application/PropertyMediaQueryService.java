package com.housekey.media.application;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.housekey.listings.api.PropertyDtos.GalleryResponse;
import com.housekey.media.domain.MediaKind;
import com.housekey.media.infrastructure.PropertyMediaEntity;
import com.housekey.media.infrastructure.PropertyMediaRepository;
import com.housekey.media.mapper.PropertyMediaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PropertyMediaQueryService {

    private final PropertyMediaRepository repository;
    private final PropertyMediaMapper mapper;

    public PropertyMediaQueryService(PropertyMediaRepository repository, PropertyMediaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<GalleryResponse> galleryResponsesOrNull(Long propertyId) {
        List<PropertyMediaEntity> media = repository
                .findByPropertyIdAndMediaKindAndDeletedAtIsNullOrderBySortOrderAscIdAsc(propertyId, MediaKind.IMAGE);
        if (media.isEmpty()) {
            return null;
        }
        return toGalleryResponses(media);
    }

    public Map<Long, List<GalleryResponse>> galleryResponsesByPropertyIds(Collection<Long> propertyIds) {
        if (propertyIds == null || propertyIds.isEmpty()) {
            return Map.of();
        }

        List<PropertyMediaEntity> media = repository
                .findByPropertyIdInAndMediaKindAndDeletedAtIsNullOrderByPropertyIdAscSortOrderAscIdAsc(
                        propertyIds,
                        MediaKind.IMAGE);
        Map<Long, List<PropertyMediaEntity>> grouped = new LinkedHashMap<>();
        for (PropertyMediaEntity item : media) {
            grouped.computeIfAbsent(item.getPropertyId(), ignored -> new java.util.ArrayList<>()).add(item);
        }

        Map<Long, List<GalleryResponse>> result = new LinkedHashMap<>();
        grouped.forEach((propertyId, items) -> result.put(propertyId, toGalleryResponses(items)));
        return result;
    }

    private List<GalleryResponse> toGalleryResponses(List<PropertyMediaEntity> media) {
        return java.util.stream.IntStream.range(0, media.size())
                .mapToObj(index -> mapper.toGalleryResponse(media.get(index), index))
                .toList();
    }
}
