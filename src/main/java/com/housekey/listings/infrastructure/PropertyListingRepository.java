package com.housekey.listings.infrastructure;

import java.util.List;

import com.housekey.listings.domain.ListingLifecycleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PropertyListingRepository
        extends JpaRepository<PropertyListingEntity, Long>, JpaSpecificationExecutor<PropertyListingEntity> {

    List<PropertyListingEntity> findByFeaturedTrueAndLifecycleStatusOrderByPublishedAtDesc(
            ListingLifecycleStatus lifecycleStatus);

    List<PropertyListingEntity> findByCityIgnoreCaseAndPropertyTypeIgnoreCaseAndIdNotAndLifecycleStatus(
            String city,
            String propertyType,
            Long id,
            ListingLifecycleStatus lifecycleStatus,
            Pageable pageable);

    List<PropertyListingEntity> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);

    List<PropertyListingEntity> findByLifecycleStatus(ListingLifecycleStatus lifecycleStatus, Sort sort);

    @Query("select distinct p.propertyType from PropertyListingEntity p order by p.propertyType")
    List<String> findDistinctPropertyTypes();

    @Query("select distinct status from PropertyListingEntity p join p.propertyStatus status order by status")
    List<String> findDistinctPropertyStatuses();

    @Query("select distinct feature from PropertyListingEntity p join p.features feature order by feature")
    List<String> findDistinctFeatures();

    @Query("select distinct p.city from PropertyListingEntity p order by p.city")
    List<String> findDistinctCities();

    @Query("select distinct p.zipCode from PropertyListingEntity p order by p.zipCode")
    List<String> findDistinctZipCodes();

    @Query("select distinct neighborhood from PropertyListingEntity p join p.neighborhood neighborhood order by neighborhood")
    List<String> findDistinctNeighborhoods();

    @Query("select distinct street from PropertyListingEntity p join p.street street order by street")
    List<String> findDistinctStreets();
}
