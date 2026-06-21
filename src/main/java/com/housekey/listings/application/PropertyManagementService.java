package com.housekey.listings.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.housekey.agents.infrastructure.AgentRepository;
import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.catalog.application.CatalogLocalizationService;
import com.housekey.listings.api.MyPropertyResponse;
import com.housekey.listings.api.PropertyCreateRequest;
import com.housekey.listings.api.PropertyDetailResponse;
import com.housekey.listings.api.PropertyUpdateRequest;
import com.housekey.listings.api.PropertyWriteSections.AdditionalSection;
import com.housekey.listings.api.PropertyWriteSections.AddressSection;
import com.housekey.listings.api.PropertyWriteSections.BasicSection;
import com.housekey.listings.api.PropertyWriteSections.MediaSection;
import com.housekey.listings.application.PropertyWriteModel.AdditionalFeatureModel;
import com.housekey.listings.application.PropertyWriteModel.FloorPlanModel;
import com.housekey.listings.application.PropertyWriteModel.GalleryImageModel;
import com.housekey.listings.application.PropertyWriteModel.PriceModel;
import com.housekey.listings.application.PropertyWriteModel.VideoModel;
import com.housekey.listings.domain.ListingLifecycleStatus;
import com.housekey.listings.domain.PropertyValidationException;
import com.housekey.listings.infrastructure.PropertyListingEntity;
import com.housekey.listings.infrastructure.PropertyListingRepository;
import com.housekey.listings.mapper.PropertyListingMapper;
import com.housekey.media.application.PropertyMediaQueryService;
import com.housekey.shared.error.LocalizedAccessDeniedException;
import com.housekey.shared.error.ResourceNotFoundException;
import com.housekey.users.domain.UserRole;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PropertyManagementService {

    private static final String DEFAULT_AREA_UNIT = "m2";
    private static final String DEFAULT_PLACEHOLDER_IMAGE = "images/props/placeholder.jpg";
    private static final int MIN_REALISTIC_YEAR = 1800;

    private final PropertyListingRepository repository;
    private final PropertyListingMapper mapper;
    private final PropertyMediaQueryService mediaQueryService;
    private final CatalogLocalizationService catalogLocalizationService;
    private final AgentRepository agentRepository;

    public PropertyManagementService(
            PropertyListingRepository repository,
            PropertyListingMapper mapper,
            PropertyMediaQueryService mediaQueryService,
            CatalogLocalizationService catalogLocalizationService,
            AgentRepository agentRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.mediaQueryService = mediaQueryService;
        this.catalogLocalizationService = catalogLocalizationService;
        this.agentRepository = agentRepository;
    }

    @Transactional(readOnly = true)
    public List<MyPropertyResponse> getMyProperties(AuthenticatedUser principal) {
        if (principal.role() == UserRole.ADMIN) {
            List<PropertyListingEntity> listings = repository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt", "id"));
            Map<Long, List<com.housekey.listings.api.PropertyDtos.GalleryResponse>> galleriesByPropertyId =
                    mediaQueryService.galleryResponsesByPropertyIds(
                            listings.stream().map(PropertyListingEntity::getId).toList());
            return listings.stream()
                    .map(entity -> mapper.toMyPropertyResponse(entity, galleriesByPropertyId.get(entity.getId())))
                    .toList();
        }

        List<PropertyListingEntity> listings = repository.findByOwnerUserIdOrderByUpdatedAtDesc(principal.id());
        Map<Long, List<com.housekey.listings.api.PropertyDtos.GalleryResponse>> galleriesByPropertyId =
                mediaQueryService.galleryResponsesByPropertyIds(
                        listings.stream().map(PropertyListingEntity::getId).toList());
        return listings.stream()
                .map(entity -> mapper.toMyPropertyResponse(entity, galleriesByPropertyId.get(entity.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyDetailResponse getMyProperty(Long id, AuthenticatedUser principal) {
        PropertyListingEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", id));
        assertCanManage(entity, principal);
        return mapper.toDetailResponse(entity, mediaQueryService.galleryResponsesOrNull(entity.getId()));
    }

    public PropertyDetailResponse create(PropertyCreateRequest request, AuthenticatedUser principal) {
        assertCanCreate(principal);

        ListingLifecycleStatus lifecycleStatus = request.lifecycleStatus() == null
                ? defaultLifecycleStatus(principal)
                : request.lifecycleStatus();
        PropertyWriteModel model = toWriteModel(
                request.basic(),
                request.address(),
                request.additional(),
                request.media(),
                lifecycleStatus);

        Long assignedAgentId = assignedAgentId(request.agentId(), principal);
        PropertyListingEntity entity = new PropertyListingEntity(principal.id(), null);
        entity.setRatingsCount(0);
        entity.setRatingsValue(0);
        entity.setViews(0);
        entity.setAgentId(assignedAgentId);
        entity.setLifecycleStatus(ListingLifecycleStatus.PUBLISHED); // Defaulted manually
        mapper.apply(entity, model);

        PropertyListingEntity saved = repository.save(entity);
        return mapper.toDetailResponse(saved, mediaQueryService.galleryResponsesOrNull(saved.getId()));
    }

    public PropertyDetailResponse update(Long id, PropertyUpdateRequest request, AuthenticatedUser principal) {
        PropertyListingEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", id));
        assertCanManage(entity, principal);

        ListingLifecycleStatus lifecycleStatus = request.lifecycleStatus() == null
                ? entity.getLifecycleStatus()
                : request.lifecycleStatus();
        PropertyWriteModel model = toWriteModel(
                request.basic(),
                request.address(),
                request.additional(),
                request.media(),
                lifecycleStatus);
        entity.setAgentId(assignedAgentId(request.agentId(), principal));
        mapper.apply(entity, model);

        PropertyListingEntity saved = repository.save(entity);
        return mapper.toDetailResponse(saved, mediaQueryService.galleryResponsesOrNull(saved.getId()));
    }

    public void archive(Long id, AuthenticatedUser principal) {
        PropertyListingEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.property.notFound", id));
        assertCanManage(entity, principal);
        entity.setLifecycleStatus(ListingLifecycleStatus.ARCHIVED);
        repository.save(entity);
    }

    private PropertyWriteModel toWriteModel(
            BasicSection basic,
            AddressSection address,
            AdditionalSection additional,
            MediaSection media,
            ListingLifecycleStatus lifecycleStatus) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        if (basic == null) {
            fieldErrors.put("basic", "validation.basic.required");
        }
        if (address == null) {
            fieldErrors.put("address", "validation.address.required");
        }

        String title = basic == null ? null : trimToNull(basic.title());
        if (title == null) {
            fieldErrors.put("basic.title", "validation.title.required");
        }

        String description = basic == null
                ? ""
                : trimToEmpty(firstNonBlank(basic.description(), basic.desc()));
        String propertyType = basic == null
                ? null
                : catalogLocalizationService.propertyTypeValue(basic.propertyType(), "basic.propertyType", fieldErrors);
        if (propertyType == null) {
            fieldErrors.putIfAbsent("basic.propertyType", "validation.propertyType.required");
        }

        List<String> listingStatuses = basic == null
                ? List.of()
                : catalogLocalizationService.propertyStatusValues(
                        firstList(basic.propertyStatus(), basic.listingStatuses(), basic.tags()),
                        "basic.propertyStatus",
                        false,
                        fieldErrors);
        PriceSide defaultPriceSide = defaultPriceSide(listingStatuses);
        PriceModel priceDollar = basic == null
                ? new PriceModel(null, null)
                : priceModel(
                        basic.priceDollar(),
                        basic.salePrice(),
                        basic.rentPrice(),
                        defaultPriceSide,
                        "basic.priceDollar",
                        fieldErrors);
        PriceModel priceEuro = basic == null
                ? new PriceModel(null, null)
                : priceModel(basic.priceEuro(), null, null, defaultPriceSide, "basic.priceEuro", fieldErrors);
        PriceModel priceDinars = basic == null
                ? new PriceModel(null, null)
                : priceModel(basic.priceDinars(), null, null, defaultPriceSide, "basic.priceDinars", fieldErrors);

        if (lifecycleStatus == ListingLifecycleStatus.PUBLISHED
                && !hasAnyPrice(priceDollar, priceEuro, priceDinars)) {
            fieldErrors.put("basic.priceDollar", "validation.price.requiredWhenPublishing");
        }

        String formattedAddress = address == null
                ? null
                : trimToNull(firstNonBlank(text(address.formattedAddress()), addressText(address.location())));
        if (formattedAddress == null) {
            fieldErrors.put("address.formattedAddress", "validation.address.formatted.required");
        }

        String city = address == null ? null : trimToNull(selectionText(address.city()));
        if (city == null) {
            fieldErrors.put("address.city", "validation.city.required");
        }

        String zipCode = address == null ? "" : trimToEmpty(text(address.zipCode()));
        List<String> neighborhoods = address == null
                ? List.of()
                : selectionTexts(firstList(address.neighborhoods(), address.neighborhood()));
        List<String> streets = address == null
                ? List.of()
                : selectionTexts(firstList(address.streets(), address.street()));
        BigDecimal latitude = address == null
                ? null
                : decimalValue(firstNode(address.lat(), child(address.location(), "lat")), "address.lat", fieldErrors);
        BigDecimal longitude = address == null
                ? null
                : decimalValue(firstNode(address.lng(), child(address.location(), "lng")), "address.lng", fieldErrors);
        validateLatitude(latitude, fieldErrors);
        validateLongitude(longitude, fieldErrors);

        Integer bedrooms = integerValue(node(additional, AdditionalSection::bedrooms), "additional.bedrooms", fieldErrors, 0);
        Integer bathrooms = integerValue(node(additional, AdditionalSection::bathrooms), "additional.bathrooms", fieldErrors, 0);
        Integer garages = integerValue(node(additional, AdditionalSection::garages), "additional.garages", fieldErrors, 0);
        Integer area = areaValue(additional == null ? null : additional.area(), "additional.area", fieldErrors);
        Integer yearBuilt = yearBuilt(node(additional, AdditionalSection::yearBuilt), fieldErrors);
        List<String> features = additional == null
                ? List.of()
                : catalogLocalizationService.featureValues(additional.features(), "additional.features", true, fieldErrors);

        List<GalleryImageModel> gallery = gallery(
                media == null ? null : media.gallery(),
                basic == null ? null : basic.gallery());
        List<VideoModel> videos = media == null ? List.of() : videos(media.videos(), fieldErrors);
        List<FloorPlanModel> plans = media == null
                ? List.of()
                : plans(firstList(media.plans(), media.floorPlans()), fieldErrors);
        List<AdditionalFeatureModel> additionalFeatures = media == null
                ? List.of()
                : additionalFeatures(media.additionalFeatures(), fieldErrors);
        boolean featured = media != null && Boolean.TRUE.equals(media.featured());

        if (!fieldErrors.isEmpty()) {
            throw new PropertyValidationException("error.property.validation", fieldErrors);
        }

        return new PropertyWriteModel(
                title,
                description,
                propertyType,
                listingStatuses,
                city,
                zipCode,
                neighborhoods,
                streets,
                latitude,
                longitude,
                formattedAddress,
                features,
                featured,
                priceDollar,
                priceEuro,
                priceDinars,
                bedrooms,
                bathrooms,
                garages,
                area,
                DEFAULT_AREA_UNIT,
                yearBuilt,
                additionalFeatures,
                gallery,
                plans,
                videos,
                lifecycleStatus);
    }

    private void assertCanCreate(AuthenticatedUser principal) {
        if (principal != null && principal.role() == UserRole.AGENCY) {
            return;
        }
        throw new LocalizedAccessDeniedException("error.accessDenied.createListing");
    }

    private void assertCanManage(PropertyListingEntity entity, AuthenticatedUser principal) {
        if (principal.role() == UserRole.ADMIN) {
            return;
        }
        if (Objects.equals(entity.getOwnerUserId(), principal.id())) {
            return;
        }
        throw new LocalizedAccessDeniedException("error.accessDenied.manageListing");
    }

    private ListingLifecycleStatus defaultLifecycleStatus(AuthenticatedUser principal) {
        return ListingLifecycleStatus.PUBLISHED;
    }

    private Long assignedAgentId(Long agentId, AuthenticatedUser principal) {
        if (agentId == null) {
            return null;
        }
        if (principal != null
                && principal.role() == UserRole.AGENCY
                && agentRepository.existsByIdAndAgencyUserIdAndActiveTrue(agentId, principal.id())) {
            return agentId;
        }
        throw new PropertyValidationException(
                "error.property.validation",
                Map.of("agentId", "validation.agent.invalid"));
    }

    private PriceModel priceModel(
            JsonNode price,
            JsonNode salePrice,
            JsonNode rentPrice,
            PriceSide defaultSide,
            String path,
            Map<String, String> fieldErrors) {
        Long sale = null;
        Long rent = null;

        if (price != null && price.isObject()) {
            sale = nonNegativeLong(firstNode(price.get("sale"), price.get("salePrice")), path + ".sale", fieldErrors);
            rent = nonNegativeLong(firstNode(price.get("rent"), price.get("rentPrice")), path + ".rent", fieldErrors);
        } else {
            Long scalar = nonNegativeLong(price, path, fieldErrors);
            if (scalar != null) {
                if (defaultSide == PriceSide.RENT) {
                    rent = scalar;
                } else {
                    sale = scalar;
                }
            }
        }

        Long explicitSale = nonNegativeLong(salePrice, "basic.salePrice", fieldErrors);
        Long explicitRent = nonNegativeLong(rentPrice, "basic.rentPrice", fieldErrors);
        if (explicitSale != null) {
            sale = explicitSale;
        }
        if (explicitRent != null) {
            rent = explicitRent;
        }

        return new PriceModel(sale, rent);
    }

    private boolean hasAnyPrice(PriceModel... prices) {
        for (PriceModel price : prices) {
            if (price.sale() != null || price.rent() != null) {
                return true;
            }
        }
        return false;
    }

    private PriceSide defaultPriceSide(List<String> statuses) {
        boolean forRent = statuses.contains("For Rent");
        boolean forSale = statuses.contains("For Sale");
        if (forRent && !forSale) {
            return PriceSide.RENT;
        }
        return PriceSide.SALE;
    }

    private Integer integerValue(JsonNode node, String path, Map<String, String> fieldErrors, int defaultValue) {
        Long value = nonNegativeLong(node, path, fieldErrors);
        if (value == null) {
            return defaultValue;
        }
        if (value > Integer.MAX_VALUE) {
            fieldErrors.put(path, "validation.value.tooLarge");
            return defaultValue;
        }
        return value.intValue();
    }

    private Integer areaValue(JsonNode node, String path, Map<String, String> fieldErrors) {
        JsonNode valueNode = node != null && node.isObject() ? node.get("value") : node;
        return integerValue(valueNode, path, fieldErrors, 0);
    }

    private Integer yearBuilt(JsonNode node, Map<String, String> fieldErrors) {
        if (isBlankNode(node)) {
            return LocalDate.now().getYear();
        }
        Integer value = integerValue(node, "additional.yearBuilt", fieldErrors, LocalDate.now().getYear());
        int maxYear = LocalDate.now().getYear() + 1;
        if (value < MIN_REALISTIC_YEAR || value > maxYear) {
            fieldErrors.put("additional.yearBuilt", "validation.yearBuilt.realistic");
        }
        return value;
    }

    private BigDecimal decimalValue(JsonNode node, String path, Map<String, String> fieldErrors) {
        if (isBlankNode(node)) {
            fieldErrors.put(path, "validation.coordinate.required");
            return null;
        }

        try {
            String raw = text(node);
            if (raw == null) {
                fieldErrors.put(path, "validation.coordinate.numeric");
                return null;
            }
            BigDecimal value = new BigDecimal(raw);
            return value.setScale(6, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            fieldErrors.put(path, "validation.coordinate.numeric");
            return null;
        }
    }

    private void validateLatitude(BigDecimal latitude, Map<String, String> fieldErrors) {
        if (latitude == null) {
            return;
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            fieldErrors.put("address.lat", "validation.latitude.range");
        }
    }

    private void validateLongitude(BigDecimal longitude, Map<String, String> fieldErrors) {
        if (longitude == null) {
            return;
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            fieldErrors.put("address.lng", "validation.longitude.range");
        }
    }

    private Long nonNegativeLong(JsonNode node, String path, Map<String, String> fieldErrors) {
        if (isBlankNode(node)) {
            return null;
        }
        try {
            String raw = text(node);
            if (raw == null) {
                fieldErrors.put(path, "validation.value.numeric");
                return null;
            }
            BigDecimal value = new BigDecimal(raw);
            if (value.scale() > 0 && value.stripTrailingZeros().scale() > 0) {
                fieldErrors.put(path, "validation.value.wholeNumber");
                return null;
            }
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                fieldErrors.put(path, "validation.value.nonNegative");
                return null;
            }
            return value.longValueExact();
        } catch (ArithmeticException | NumberFormatException ex) {
            fieldErrors.put(path, "validation.value.numeric");
            return null;
        }
    }

    private List<GalleryImageModel> gallery(List<JsonNode> mediaGallery, List<JsonNode> basicGallery) {
        List<JsonNode> nodes = firstList(mediaGallery, basicGallery);
        List<GalleryImageModel> images = new ArrayList<>();
        if (nodes != null) {
            for (JsonNode node : nodes) {
                String medium = imageUrl(node);
                if (medium != null) {
                    String small = firstNonBlank(textField(node, "small"), textField(node, "smallUrl"), medium);
                    String big = firstNonBlank(textField(node, "big"), textField(node, "bigUrl"), medium);
                    images.add(new GalleryImageModel(small, medium, big));
                }
            }
        }

        if (images.isEmpty()) {
            images.add(new GalleryImageModel(
                    DEFAULT_PLACEHOLDER_IMAGE,
                    DEFAULT_PLACEHOLDER_IMAGE,
                    DEFAULT_PLACEHOLDER_IMAGE));
        }
        return List.copyOf(images);
    }

    private List<VideoModel> videos(List<JsonNode> nodes, Map<String, String> fieldErrors) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<VideoModel> videos = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            String name = trimToNull(textField(node, "name"));
            String link = trimToNull(textField(node, "link"));
            if (name == null && link == null) {
                continue;
            }
            if (name == null || link == null) {
                fieldErrors.put("media.videos[" + i + "]", "validation.video.nameAndLinkRequired");
                continue;
            }
            videos.add(new VideoModel(name, link));
        }
        return List.copyOf(videos);
    }

    private List<FloorPlanModel> plans(List<JsonNode> nodes, Map<String, String> fieldErrors) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<FloorPlanModel> plans = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            String name = trimToNull(textField(node, "name"));
            String desc = trimToEmpty(textField(node, "desc"));
            Integer area = areaValue(child(node, "area"), "media.plans[" + i + "].area", fieldErrors);
            Integer rooms = integerValue(child(node, "rooms"), "media.plans[" + i + "].rooms", fieldErrors, 0);
            Integer baths = integerValue(child(node, "baths"), "media.plans[" + i + "].baths", fieldErrors, 0);
            String image = imageUrl(child(node, "image"));

            if (name == null && desc.isBlank() && area == 0 && rooms == 0 && baths == 0 && image == null) {
                continue;
            }
            if (name == null) {
                fieldErrors.put("media.plans[" + i + "].name", "validation.plan.nameRequired");
                continue;
            }
            plans.add(new FloorPlanModel(
                    name,
                    desc,
                    area,
                    DEFAULT_AREA_UNIT,
                    rooms,
                    baths,
                    image == null ? DEFAULT_PLACEHOLDER_IMAGE : image));
        }
        return List.copyOf(plans);
    }

    private List<AdditionalFeatureModel> additionalFeatures(List<JsonNode> nodes, Map<String, String> fieldErrors) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<AdditionalFeatureModel> features = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            String name = trimToNull(textField(node, "name"));
            String value = trimToNull(textField(node, "value"));
            if (name == null && value == null) {
                continue;
            }
            if (name == null || value == null) {
                fieldErrors.put(
                        "media.additionalFeatures[" + i + "]",
                        "validation.additionalFeature.nameAndValueRequired");
                continue;
            }
            features.add(new AdditionalFeatureModel(name, value));
        }
        return List.copyOf(features);
    }

    @SafeVarargs
    private final List<JsonNode> firstList(List<JsonNode>... lists) {
        for (List<JsonNode> list : lists) {
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        return List.of();
    }

    private List<String> selectionTexts(List<JsonNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        nodes.stream()
                .map(this::selectionText)
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .forEach(values::add);
        return List.copyOf(values);
    }

    private String selectionText(JsonNode node) {
        if (isBlankNode(node)) {
            return null;
        }
        if (node.isObject()) {
            return firstNonBlank(
                    textField(node, "name"),
                    textField(node, "title"),
                    textField(node, "value"),
                    textField(node, "label"));
        }
        return text(node);
    }

    private String addressText(JsonNode node) {
        if (isBlankNode(node)) {
            return null;
        }
        if (node.isObject()) {
            return firstNonBlank(
                    textField(node, "formattedAddress"),
                    textField(node, "formatted_address"),
                    textField(node, "address"),
                    textField(node, "location"),
                    textField(node, "name"));
        }
        return text(node);
    }

    private String imageUrl(JsonNode node) {
        JsonNode imageNode = node;
        if (imageNode != null && imageNode.isArray() && imageNode.size() > 0) {
            imageNode = imageNode.get(0);
        }
        if (isBlankNode(imageNode)) {
            return null;
        }
        if (!imageNode.isObject()) {
            return trimToNull(text(imageNode));
        }
        return trimToNull(firstNonBlank(
                textField(imageNode, "medium"),
                textField(imageNode, "mediumUrl"),
                textField(imageNode, "link"),
                textField(imageNode, "preview"),
                textField(imageNode, "url"),
                textField(imageNode, "big"),
                textField(imageNode, "small")));
    }

    private JsonNode child(JsonNode node, String fieldName) {
        if (node == null || !node.isObject()) {
            return null;
        }
        return node.get(fieldName);
    }

    private JsonNode firstNode(JsonNode first, JsonNode second) {
        return !isBlankNode(first) ? first : second;
    }

    private String textField(JsonNode node, String fieldName) {
        JsonNode child = child(node, fieldName);
        return text(child);
    }

    private String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        return null;
    }

    private boolean isBlankNode(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return true;
        }
        return node.isTextual() && node.asText().isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private JsonNode node(AdditionalSection section, SectionNodeExtractor extractor) {
        if (section == null) {
            return null;
        }
        return extractor.get(section);
    }

    private enum PriceSide {
        SALE,
        RENT
    }

    @FunctionalInterface
    private interface SectionNodeExtractor {
        JsonNode get(AdditionalSection section);
    }
}
