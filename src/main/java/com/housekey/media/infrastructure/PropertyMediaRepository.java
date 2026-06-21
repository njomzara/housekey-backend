package com.housekey.media.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.housekey.media.domain.MediaKind;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyMediaRepository extends JpaRepository<PropertyMediaEntity, Long> {

    @EntityGraph(attributePaths = "assets")
    List<PropertyMediaEntity> findByPropertyIdAndDeletedAtIsNullOrderBySortOrderAscIdAsc(Long propertyId);

    @EntityGraph(attributePaths = "assets")
    List<PropertyMediaEntity> findByPropertyIdAndMediaKindAndDeletedAtIsNullOrderBySortOrderAscIdAsc(
            Long propertyId,
            MediaKind mediaKind);

    @EntityGraph(attributePaths = "assets")
    List<PropertyMediaEntity> findByPropertyIdInAndMediaKindAndDeletedAtIsNullOrderByPropertyIdAscSortOrderAscIdAsc(
            Collection<Long> propertyIds,
            MediaKind mediaKind);

    @EntityGraph(attributePaths = "assets")
    Optional<PropertyMediaEntity> findByIdAndPropertyIdAndDeletedAtIsNull(Long id, Long propertyId);

    @Query("""
            select coalesce(max(media.sortOrder), -1)
            from PropertyMediaEntity media
            where media.propertyId = :propertyId
              and media.mediaKind = :mediaKind
              and media.deletedAt is null
            """)
    int findMaxSortOrder(
            @Param("propertyId") Long propertyId,
            @Param("mediaKind") MediaKind mediaKind);
}
