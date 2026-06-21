package com.housekey.catalog.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import com.housekey.catalog.api.CatalogDtos.CatalogItemResponse;
import com.housekey.catalog.api.CatalogDtos.LocationsResponse;
import com.housekey.catalog.api.CatalogDtos.NeighborhoodResponse;
import com.housekey.catalog.api.CatalogDtos.PropertyLocationResponse;
import com.housekey.catalog.api.CatalogDtos.StreetResponse;
import com.housekey.listings.domain.ListingLifecycleStatus;
import com.housekey.listings.infrastructure.PropertyListingEntity;
import com.housekey.listings.infrastructure.PropertyListingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

    private static final int NOVI_SAD_CITY_ID = 1;

    private static final List<String> CITY_NAMES = List.of(
            "Novi Sad",
            "Futog",
            "Veternik",
            "Begec",
            "Kac",
            "Rumenka",
            "Kovilj",
            "Sremski Karlovci",
            "Temerin",
            "Beocin",
            "Backi Petrovac",
            "Zabalj",
            "Titel",
            "Srbobran",
            "Backa Palanka",
            "Indjija");

    private static final List<String> NOVI_SAD_NEIGHBORHOOD_NAMES = List.of(
            "Adamovicevo Naselje",
            "Adice",
            "Almaski Kraj",
            "Avijaticarsko Naselje",
            "Banatic",
            "Bistrica",
            "Bulevar",
            "Depresija",
            "Detelinara",
            "Gornje Livade",
            "Grbavica",
            "Jugovicevo",
            "Klisa",
            "Liman I",
            "Liman II",
            "Liman III",
            "Liman IV",
            "Mali Beograd",
            "Misin Salas",
            "Novo Naselje",
            "Pervazovo Naselje",
            "Podbara",
            "Rimski Sancevi",
            "Rotkvarija",
            "Sajlovo",
            "Sajmiste",
            "Salajka",
            "Satelit",
            "Slana Bara",
            "Stari Grad",
            "Sangaj",
            "Tankosicevo",
            "Telep",
            "Univerzitetski Kampus",
            "Veliki Rit",
            "Veternicka Rampa",
            "Vidovdansko Naselje",
            "Petrovaradin",
            "Petrovaradinska Tvrdjava",
            "Podgradje Tvrdjave",
            "Alibegovac",
            "Bukovacki Plato",
            "Miseluk",
            "Novi Majur",
            "Ribnjak",
            "Sadovi",
            "Sirine",
            "Siroka Dolina",
            "Stari Majur",
            "Trandzament",
            "Vezirac",
            "Sremska Kamenica",
            "Artiljevo",
            "Bocke",
            "Cardak",
            "Donja Kamenica",
            "Glavica",
            "Gornja Kamenica",
            "Paragovo",
            "Popovica",
            "Staroiriski Put",
            "Tatarsko Brdo");

    private static final List<CatalogItemResponse> CITIES = catalogItems(CITY_NAMES);
    private static final List<NeighborhoodResponse> NEIGHBORHOODS = neighborhoods();
    private static final List<StreetResponse> STREETS = streets();

    private final PropertyListingRepository repository;
    private final CatalogLocalizationService catalogLocalizationService;

    public CatalogQueryService(
            PropertyListingRepository repository,
            CatalogLocalizationService catalogLocalizationService) {
        this.repository = repository;
        this.catalogLocalizationService = catalogLocalizationService;
    }

    public List<CatalogItemResponse> propertyTypes() {
        return catalogLocalizationService.propertyTypes();
    }

    public List<CatalogItemResponse> propertyStatuses() {
        return catalogLocalizationService.propertyStatuses();
    }

    public List<com.housekey.catalog.api.CatalogDtos.FeatureResponse> features() {
        return catalogLocalizationService.features();
    }

    public LocationsResponse locations() {
        List<PropertyListingEntity> listings = repository.findByLifecycleStatus(
                ListingLifecycleStatus.PUBLISHED,
                Sort.by("id"));
        List<CatalogItemResponse> zipCodes = toCatalogItems(repository.findDistinctZipCodes());
        List<PropertyLocationResponse> propertyLocations = listings.stream()
                .sorted(Comparator.comparing(PropertyListingEntity::getId))
                .map(listing -> new PropertyLocationResponse(
                        listing.getId(),
                        listing.getLocation().getLat(),
                        listing.getLocation().getLng()))
                .toList();

        return new LocationsResponse(CITIES, zipCodes, NEIGHBORHOODS, STREETS, propertyLocations);
    }

    private List<CatalogItemResponse> toCatalogItems(List<String> names) {
        return catalogItems(names);
    }

    private static List<CatalogItemResponse> catalogItems(List<String> names) {
        return IntStream.range(0, names.size())
                .mapToObj(index -> new CatalogItemResponse(index + 1, names.get(index)))
                .toList();
    }

    private static List<NeighborhoodResponse> neighborhoods() {
        List<NeighborhoodResponse> neighborhoods = new ArrayList<>();
        int id = 1;
        for (String name : NOVI_SAD_NEIGHBORHOOD_NAMES) {
            neighborhoods.add(new NeighborhoodResponse(id++, name, NOVI_SAD_CITY_ID));
        }
        for (int cityIndex = 1; cityIndex < CITY_NAMES.size(); cityIndex++) {
            neighborhoods.add(new NeighborhoodResponse(id++, CITY_NAMES.get(cityIndex), cityIndex + 1));
        }
        return List.copyOf(neighborhoods);
    }

    private static List<StreetResponse> streets() {
        return IntStream.range(0, NEIGHBORHOODS.size())
                .mapToObj(index -> {
                    NeighborhoodResponse neighborhood = NEIGHBORHOODS.get(index);
                    return new StreetResponse(
                            index + 1,
                            streetName(neighborhood.name()),
                            neighborhood.cityId(),
                            neighborhood.id());
                })
                .toList();
    }

    private static String streetName(String neighborhoodName) {
        return switch (neighborhoodName) {
            case "Stari Grad" -> "Zmaj Jovina";
            case "Grbavica" -> "Bulevar oslobodjenja";
            case "Detelinara" -> "Bulevar Evrope";
            case "Liman I", "Liman II", "Liman III", "Liman IV" -> "Narodnog fronta";
            case "Telep" -> "Futoski put";
            case "Podbara" -> "Kraljevica Marka";
            case "Rotkvarija" -> "Kisacka";
            case "Sajmiste" -> "Hajduk Veljkova";
            case "Petrovaradin" -> "Preradoviceva";
            case "Sremska Kamenica" -> "Vojvode Putnika";
            default -> "Glavna ulica";
        };
    }

}
