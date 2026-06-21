package com.housekey.listings.api;

import com.housekey.listings.api.PropertyWriteSections.AdditionalSection;
import com.housekey.listings.api.PropertyWriteSections.AddressSection;
import com.housekey.listings.api.PropertyWriteSections.BasicSection;
import com.housekey.listings.api.PropertyWriteSections.MediaSection;
import com.housekey.listings.domain.ListingLifecycleStatus;
import jakarta.validation.Valid;

public record PropertyUpdateRequest(
        @Valid BasicSection basic,
        @Valid AddressSection address,
        @Valid AdditionalSection additional,
        @Valid MediaSection media,
        ListingLifecycleStatus lifecycleStatus,
        Long agentId) {
}
