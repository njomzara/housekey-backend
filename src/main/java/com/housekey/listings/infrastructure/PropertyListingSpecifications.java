package com.housekey.listings.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.housekey.listings.domain.ListingLifecycleStatus;
import com.housekey.listings.domain.CurrencyCode;
import com.housekey.listings.domain.PropertySearchCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public final class PropertyListingSpecifications {

    private PropertyListingSpecifications() {
    }

    public static Specification<PropertyListingEntity> matching(PropertySearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(needsDistinct(criteria));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("lifecycleStatus"), ListingLifecycleStatus.PUBLISHED));
            addEqualsIgnoreCase(predicates, cb, root.get("propertyType"), criteria.propertyType());
            addEqualsIgnoreCase(predicates, cb, root.get("city"), criteria.city());
            addEqualsIgnoreCase(predicates, cb, root.get("zipCode"), criteria.zipCode());
            addRange(predicates, cb, root.get("bedrooms"), criteria.bedroomsFrom(), criteria.bedroomsTo());
            addRange(predicates, cb, root.get("bathrooms"), criteria.bathroomsFrom(), criteria.bathroomsTo());
            addRange(predicates, cb, root.get("garages"), criteria.garagesFrom(), criteria.garagesTo());
            addRange(predicates, cb, root.get("area").get("value"), criteria.areaFrom(), criteria.areaTo());
            addRange(predicates, cb, root.get("yearBuilt"), criteria.yearBuiltFrom(), criteria.yearBuiltTo());
            if (criteria.agentId() != null) {
                predicates.add(cb.equal(root.get("agentId"), criteria.agentId()));
            }

            if (!criteria.propertyStatus().isEmpty()) {
                Join<PropertyListingEntity, String> statusJoin = root.join("propertyStatus", JoinType.INNER);
                predicates.add(lowerIn(cb, statusJoin, criteria.propertyStatus()));
            }

            if (!criteria.neighborhood().isEmpty()) {
                Join<PropertyListingEntity, String> neighborhoodJoin = root.join("neighborhood", JoinType.INNER);
                predicates.add(lowerIn(cb, neighborhoodJoin, criteria.neighborhood()));
            }

            if (!criteria.street().isEmpty()) {
                Join<PropertyListingEntity, String> streetJoin = root.join("street", JoinType.INNER);
                predicates.add(lowerIn(cb, streetJoin, criteria.street()));
            }

            if (!criteria.features().isEmpty()) {
                Join<PropertyListingEntity, String> featureJoin = root.join("features", JoinType.INNER);
                predicates.add(lowerIn(cb, featureJoin, criteria.features()));
            }

            if (criteria.priceFrom() != null || criteria.priceTo() != null) {
                predicates.add(pricePredicate(root, cb, criteria.currency(), criteria.priceFrom(), criteria.priceTo()));
            }

            applySorting(root, query, cb, criteria.sort(), criteria.currency());
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean needsDistinct(PropertySearchCriteria criteria) {
        return !criteria.propertyStatus().isEmpty()
                || !criteria.neighborhood().isEmpty()
                || !criteria.street().isEmpty()
                || !criteria.features().isEmpty();
    }

    private static void addEqualsIgnoreCase(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<String> path,
            String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.equal(cb.lower(path), value.trim().toLowerCase(Locale.ROOT)));
        }
    }

    private static <N extends Number & Comparable<N>> void addRange(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<N> path,
            N from,
            N to) {
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(path, to));
        }
    }

    private static Predicate lowerIn(CriteriaBuilder cb, Expression<String> expression, List<String> values) {
        List<String> normalized = values.stream()
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .toList();
        return cb.lower(expression).in(normalized);
    }

    private static Predicate pricePredicate(
            Root<PropertyListingEntity> root,
            CriteriaBuilder cb,
            CurrencyCode currency,
            Long from,
            Long to) {
        Path<Long> sale = pricePath(root, currency, "sale");
        Path<Long> rent = pricePath(root, currency, "rent");

        return cb.or(
                priceSidePredicate(cb, sale, from, to),
                priceSidePredicate(cb, rent, from, to));
    }

    private static Predicate priceSidePredicate(CriteriaBuilder cb, Path<Long> path, Long from, Long to) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNotNull(path));
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(path, to));
        }
        return cb.and(predicates.toArray(Predicate[]::new));
    }

    private static Path<Long> pricePath(Root<PropertyListingEntity> root, CurrencyCode currency, String side) {
        return switch (currency) {
            case USD -> root.get("priceDollar").get(side);
            case EUR -> root.get("priceEuro").get(side);
            case RSD -> root.get("priceDinars").get(side);
        };
    }

    private static void applySorting(
            Root<PropertyListingEntity> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String sort,
            CurrencyCode currency) {
        if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
            return;
        }

        String normalized = sort == null ? "" : sort.trim().toLowerCase(Locale.ROOT);
        Order order = switch (normalized) {
            case "newest", "published,desc", "publishedat,desc" -> cb.desc(root.get("publishedAt"));
            case "oldest", "published,asc", "publishedat,asc" -> cb.asc(root.get("publishedAt"));
            case "popular", "rating,desc", "ratings,desc" ->
                    cb.desc(cb.quot(root.get("ratingsValue").as(Double.class), root.get("ratingsCount").as(Double.class)));
            case "price (low to high)", "price,asc" -> cb.asc(priceSortExpression(root, cb, currency));
            case "price (high to low)", "price,desc" -> cb.desc(priceSortExpression(root, cb, currency));
            case "title,asc" -> cb.asc(cb.lower(root.get("title")));
            case "title,desc" -> cb.desc(cb.lower(root.get("title")));
            default -> cb.asc(root.get("id"));
        };
        query.orderBy(order, cb.asc(root.get("id")));
    }

    private static Expression<Long> priceSortExpression(
            Root<PropertyListingEntity> root,
            CriteriaBuilder cb,
            CurrencyCode currency) {
        CriteriaBuilder.Coalesce<Long> coalesce = cb.coalesce();
        coalesce.value(pricePath(root, currency, "sale"));
        coalesce.value(pricePath(root, currency, "rent"));
        coalesce.value(Long.MAX_VALUE);
        return coalesce;
    }
}
