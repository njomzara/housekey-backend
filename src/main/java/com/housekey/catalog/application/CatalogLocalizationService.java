package com.housekey.catalog.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.housekey.catalog.api.CatalogDtos.CatalogItemResponse;
import com.housekey.catalog.api.CatalogDtos.FeatureResponse;
import com.housekey.listings.domain.PropertySearchCriteria;
import com.housekey.shared.i18n.LocalizedMessages;
import org.springframework.stereotype.Service;

@Service
public class CatalogLocalizationService {

    private static final List<CatalogEntry> PROPERTY_TYPES = List.of(
            new CatalogEntry(1, "OFFICE", "Office", "catalog.propertyType.office"),
            new CatalogEntry(2, "HOUSE", "House", "catalog.propertyType.house"),
            new CatalogEntry(3, "APARTMENT", "Apartment", "catalog.propertyType.apartment"),
            new CatalogEntry(4, "GARAGE", "Garage", "catalog.propertyType.garage"),
            new CatalogEntry(5, "WEEKEND_HOUSE", "Weekend House", "catalog.propertyType.weekendHouse"));

    private static final List<CatalogEntry> PROPERTY_STATUSES = List.of(
            new CatalogEntry(1, "FOR_SALE", "For Sale", "catalog.propertyStatus.forSale"),
            new CatalogEntry(2, "FOR_RENT", "For Rent", "catalog.propertyStatus.forRent"),
            new CatalogEntry(3, "OPEN_HOUSE", "Open House", "catalog.propertyStatus.openHouse"),
            new CatalogEntry(4, "NO_FEES", "No Fees", "catalog.propertyStatus.noFees"),
            new CatalogEntry(5, "HOT_OFFER", "Hot Offer", "catalog.propertyStatus.hotOffer"),
            new CatalogEntry(6, "SOLD", "Sold", "catalog.propertyStatus.sold"));

    private static final List<CatalogEntry> FEATURES = List.of(
            new CatalogEntry(1, "AIR_CONDITIONING", "Air Conditioning", "catalog.feature.airConditioning"),
            new CatalogEntry(2, "BARBEQUE", "Barbeque", "catalog.feature.barbeque"),
            new CatalogEntry(3, "DRYER", "Dryer", "catalog.feature.dryer"),
            new CatalogEntry(4, "MICROWAVE", "Microwave", "catalog.feature.microwave"),
            new CatalogEntry(5, "REFRIGERATOR", "Refrigerator", "catalog.feature.refrigerator"),
            new CatalogEntry(6, "TV_CABLE", "TV Cable", "catalog.feature.tvCable"),
            new CatalogEntry(7, "SAUNA", "Sauna", "catalog.feature.sauna"),
            new CatalogEntry(8, "WIFI", "WiFi", "catalog.feature.wifi"),
            new CatalogEntry(9, "FIREPLACE", "Fireplace", "catalog.feature.fireplace"),
            new CatalogEntry(10, "SWIMMING_POOL", "Swimming Pool", "catalog.feature.swimmingPool"),
            new CatalogEntry(11, "GYM", "Gym", "catalog.feature.gym"));

    private final LocalizedMessages messages;

    public CatalogLocalizationService(LocalizedMessages messages) {
        this.messages = messages;
    }

    public List<CatalogItemResponse> propertyTypes() {
        return PROPERTY_TYPES.stream()
                .map(entry -> new CatalogItemResponse(entry.id(), entry.code(), label(entry)))
                .toList();
    }

    public List<CatalogItemResponse> propertyStatuses() {
        return PROPERTY_STATUSES.stream()
                .map(entry -> new CatalogItemResponse(entry.id(), entry.code(), label(entry)))
                .toList();
    }

    public List<FeatureResponse> features() {
        return FEATURES.stream()
                .map(entry -> new FeatureResponse(entry.id(), entry.code(), label(entry), false))
                .toList();
    }

    public String propertyTypeValue(JsonNode node, String path, Map<String, String> fieldErrors) {
        return catalogValue(node, PROPERTY_TYPES, path, fieldErrors);
    }

    public List<String> propertyStatusValues(
            List<JsonNode> nodes,
            String path,
            boolean selectedOnly,
            Map<String, String> fieldErrors) {
        return catalogValues(nodes, PROPERTY_STATUSES, path, selectedOnly, fieldErrors);
    }

    public List<String> featureValues(
            List<JsonNode> nodes,
            String path,
            boolean selectedOnly,
            Map<String, String> fieldErrors) {
        return catalogValues(nodes, FEATURES, path, selectedOnly, fieldErrors);
    }

    public PropertySearchCriteria canonicalize(PropertySearchCriteria criteria) {
        return new PropertySearchCriteria(
                criteria.page(),
                criteria.size(),
                criteria.sort(),
                canonicalValueOrOriginal(criteria.propertyType(), PROPERTY_TYPES),
                canonicalValuesOrOriginal(criteria.propertyStatus(), PROPERTY_STATUSES),
                criteria.city(),
                criteria.zipCode(),
                criteria.neighborhood(),
                criteria.street(),
                criteria.priceFrom(),
                criteria.priceTo(),
                criteria.currency(),
                criteria.bedroomsFrom(),
                criteria.bedroomsTo(),
                criteria.bathroomsFrom(),
                criteria.bathroomsTo(),
                criteria.garagesFrom(),
                criteria.garagesTo(),
                criteria.areaFrom(),
                criteria.areaTo(),
                criteria.yearBuiltFrom(),
                criteria.yearBuiltTo(),
                canonicalValuesOrOriginal(criteria.features(), FEATURES),
                criteria.agentId());
    }

    public String localizePropertyType(String canonicalName) {
        return localizedValue(PROPERTY_TYPES, canonicalName);
    }

    public List<String> localizePropertyStatuses(List<String> canonicalNames) {
        return canonicalNames.stream()
                .map(value -> localizedValue(PROPERTY_STATUSES, value))
                .toList();
    }

    public List<String> localizeFeatures(List<String> canonicalNames) {
        return canonicalNames.stream()
                .map(value -> localizedValue(FEATURES, value))
                .toList();
    }

    private List<String> catalogValues(
            List<JsonNode> nodes,
            List<CatalogEntry> entries,
            String path,
            boolean selectedOnly,
            Map<String, String> fieldErrors) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            JsonNode node = nodes.get(i);
            if (selectedOnly && node != null && node.isObject() && node.has("selected") && !node.get("selected").asBoolean()) {
                continue;
            }
            String value = catalogValue(node, entries, path + "[" + i + "]", fieldErrors);
            if (value != null) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private String catalogValue(
            JsonNode node,
            List<CatalogEntry> entries,
            String path,
            Map<String, String> fieldErrors) {
        if (isBlankNode(node)) {
            return null;
        }

        Integer id = catalogId(node);
        String code = selectionCode(node);
        String text = selectionText(node);

        if (id != null) {
            CatalogEntry entry = findById(entries, id);
            if (entry == null) {
                fieldErrors.put(path, "validation.catalog.invalidId");
                return null;
            }
            if ((code != null && !matches(entry, code)) || (text != null && !matches(entry, text))) {
                fieldErrors.put(path, "validation.catalog.mismatch");
                return null;
            }
            return entry.canonicalName();
        }

        CatalogEntry entry = resolve(entries, firstNonBlank(code, text));
        if (entry != null) {
            return entry.canonicalName();
        }

        if (code != null || text != null) {
            fieldErrors.put(path, "validation.catalog.invalidValue");
            return null;
        }

        fieldErrors.put(path, "validation.catalog.invalidSelection");
        return null;
    }

    private String canonicalValueOrOriginal(String value, List<CatalogEntry> entries) {
        CatalogEntry entry = resolve(entries, value);
        return entry == null ? value : entry.canonicalName();
    }

    private List<String> canonicalValuesOrOriginal(List<String> values, List<CatalogEntry> entries) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(value -> canonicalValueOrOriginal(value, entries))
                .filter(Objects::nonNull)
                .toList();
    }

    private String localizedValue(List<CatalogEntry> entries, String canonicalName) {
        CatalogEntry entry = resolve(entries, canonicalName);
        return entry == null ? canonicalName : label(entry);
    }

    private CatalogEntry resolve(List<CatalogEntry> entries, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        return entries.stream()
                .filter(entry -> matchesNormalized(entry, normalized))
                .findFirst()
                .orElse(null);
    }

    private CatalogEntry findById(List<CatalogEntry> entries, int id) {
        return entries.stream()
                .filter(entry -> entry.id() == id)
                .findFirst()
                .orElse(null);
    }

    private boolean matches(CatalogEntry entry, String value) {
        return matchesNormalized(entry, normalize(value));
    }

    private boolean matchesNormalized(CatalogEntry entry, String normalizedValue) {
        return normalize(entry.code()).equals(normalizedValue)
                || normalize(entry.canonicalName()).equals(normalizedValue)
                || normalize(label(entry)).equals(normalizedValue);
    }

    private String label(CatalogEntry entry) {
        return messages.get(entry.messageKey());
    }

    private Integer catalogId(JsonNode node) {
        JsonNode idNode = node != null && node.isObject() ? node.get("id") : node;
        if (isBlankNode(idNode)) {
            return null;
        }
        if (idNode.isNumber()) {
            return idNode.asInt();
        }
        if (idNode.isTextual()) {
            try {
                return Integer.valueOf(idNode.asText().trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String selectionCode(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        return firstNonBlank(
                textField(node, "code"),
                textField(node, "key"));
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

    private String textField(JsonNode node, String fieldName) {
        JsonNode child = node == null || !node.isObject() ? null : node.get(fieldName);
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
        return node == null
                || node.isNull()
                || node.isMissingNode()
                || (node.isTextual() && node.asText().isBlank());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private record CatalogEntry(
            int id,
            String code,
            String canonicalName,
            String messageKey) {
    }
}
