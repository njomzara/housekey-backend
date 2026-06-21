package com.housekey.listings.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PropertyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void propertyListReturnsSeededData() throws Exception {
        mockMvc.perform(get("/api/v1/properties").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(20)))
                .andExpect(jsonPath("$.pagination.total").value(20))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Moderan stan na Grbavici"));
    }

    @Test
    void propertyDetailReturnsOneProperty() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Moderan stan na Grbavici"))
                .andExpect(jsonPath("$.gallery", hasSize(4)))
                .andExpect(jsonPath("$.features", hasItem("Klima uredjaj")));
    }

    @Test
    void propertyDetailReturnsEnglishCatalogLabelsWhenRequested() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{id}", 1)
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyType").value("Apartment"))
                .andExpect(jsonPath("$.features", hasItem("Air Conditioning")));
    }

    @Test
    void searchFiltersByCityTypePriceAndFeature() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Novi Sad")
                        .param("propertyType", "Stan")
                        .param("priceFrom", "5000")
                        .param("priceTo", "6000")
                        .param("currency", "USD")
                        .param("features", "Teretana")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(8))
                .andExpect(jsonPath("$.data[0].priceDollar.rent").value(5600));
    }

    @Test
    void searchFiltersByAssignedAgent() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("agentId", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.total", greaterThan(0)))
                .andExpect(jsonPath("$.data[0].agentId").value(1));
    }

    @Test
    void searchAcceptsLocalizedCatalogLabels() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("city", "Novi Sad")
                        .param("propertyType", "Stan")
                        .param("features", "Teretana")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[*].propertyType", hasItem("Stan")));
    }

    @Test
    void catalogReturnsStableCodesAndLocalizedNames() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/property-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].code").value("APARTMENT"))
                .andExpect(jsonPath("$[2].name").value("Stan"));

        mockMvc.perform(get("/api/v1/catalog/property-types")
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].code").value("APARTMENT"))
                .andExpect(jsonPath("$[2].name").value("Apartment"));
    }

    @Test
    void searchSortsByPriceAscending() throws Exception {
        mockMvc.perform(get("/api/v1/properties")
                        .param("sort", "Price (Low to High)")
                        .param("currency", "USD")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].id").value(8))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[2].id").value(6));
    }

    @Test
    void unauthenticatedCreateFails() throws Exception {
        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyRequest(
                                "Unauthenticated listing",
                                "PENDING_REVIEW"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void agencyCreateCanAssignOwnedAgent() throws Exception {
        String token = registerAndGetToken();
        Long agentId = createAgent(token, "Assigned Agent");
        Map<String, Object> request = propertyRequest("Agency assigned listing", "PENDING_REVIEW");
        request.put("agentId", agentId);

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(20)))
                .andExpect(jsonPath("$.title").value("Agency assigned listing"))
                .andExpect(jsonPath("$.lifecycleStatus").value("PENDING_REVIEW"))
                .andExpect(jsonPath("$.agentId").value(agentId))
                .andExpect(jsonPath("$.ownerUserId").exists());
    }

    @Test
    void agencyCannotAssignAnotherAgencysAgent() throws Exception {
        String firstToken = registerAndGetToken();
        String secondToken = registerAndGetToken();
        Long foreignAgentId = createAgent(firstToken, "Foreign Agent");
        Map<String, Object> request = propertyRequest("Foreign assigned listing", "PENDING_REVIEW");
        request.put("agentId", foreignAgentId);

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + secondToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.agentId").value("Dodeljeni agent mora da pripada vasoj agenciji."));
    }

    @Test
    void agencyCreateDefaultsToPublishedWhenLifecycleStatusIsMissing() throws Exception {
        String token = registerAndGetToken();
        Map<String, Object> request = propertyRequest("Agency created listing", null);
        request.remove("lifecycleStatus");

        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Agency created listing"))
                .andExpect(jsonPath("$.lifecycleStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.ownerUserId").exists())
                .andReturn();

        Long propertyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidCatalogIdsReturnFieldErrors() throws Exception {
        String token = registerAndGetToken();
        Map<String, Object> request = propertyRequest("Invalid catalog listing", "PENDING_REVIEW");
        request.put("basic", Map.of(
                "title", "Invalid catalog listing",
                "description", "Integracioni test oglas",
                "propertyType", Map.of("id", 999, "name", "Stan"),
                "propertyStatus", List.of(Map.of("id", 999, "name", "Na prodaju")),
                "priceDollar", Map.of("sale", 250000)));
        request.put("additional", Map.of(
                "bedrooms", 2,
                "bathrooms", 1,
                "garages", 0,
                "area", 900,
                "yearBuilt", 2018,
                "features", List.of(Map.of("id", 999, "name", "Klima uredjaj", "selected", true))));

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['basic.propertyType']").value("Neispravan ID kataloga."))
                .andExpect(jsonPath("$.fieldErrors['basic.propertyStatus[0]']").value("Neispravan ID kataloga."))
                .andExpect(jsonPath("$.fieldErrors['additional.features[0]']").value("Neispravan ID kataloga."));
    }

    @Test
    void publishingWithoutSaleOrRentPriceReturnsFieldError() throws Exception {
        String token = registerAndGetToken();
        Map<String, Object> request = propertyRequest("No price listing", "PUBLISHED");
        request.put("basic", Map.of(
                "title", "No price listing",
                "description", "Integracioni test oglas",
                "propertyType", Map.of("id", 3, "name", "Stan"),
                "propertyStatus", List.of(Map.of("id", 1, "name", "Na prodaju"))));

        mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['basic.priceDollar']")
                        .value("Bar jedna prodajna ili zakupna cena je obavezna pri objavljivanju."));
    }

    @Test
    void ownerCanUpdateOwnListing() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Owner update listing", "PENDING_REVIEW");

        mockMvc.perform(put("/api/v1/properties/{id}", propertyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyRequest(
                                "Updated owner listing",
                                "PENDING_REVIEW"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propertyId))
                .andExpect(jsonPath("$.title").value("Updated owner listing"));
    }

    @Test
    void anotherUserCannotUpdateListing() throws Exception {
        String ownerToken = registerAndGetToken();
        String otherToken = registerAndGetToken();
        Long propertyId = createProperty(ownerToken, "Protected listing", "PENDING_REVIEW");

        mockMvc.perform(put("/api/v1/properties/{id}", propertyId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyRequest(
                                "Intrusive update",
                                "PENDING_REVIEW"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanArchiveOwnListingAndItLeavesPublicList() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Archive me listing", "PUBLISHED");

        mockMvc.perform(delete("/api/v1/properties/{id}", propertyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/properties/{id}", propertyId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/properties").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + propertyId + ")]", hasSize(0)));

        mockMvc.perform(get("/api/v1/me/properties/{id}", propertyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifecycleStatus").value("ARCHIVED"));
    }

    @Test
    void myPropertiesReturnsOnlyCurrentUserListings() throws Exception {
        String firstToken = registerAndGetToken();
        String secondToken = registerAndGetToken();
        Long firstPropertyId = createProperty(firstToken, "First agent listing", "PENDING_REVIEW");
        Long secondPropertyId = createProperty(secondToken, "Second agent listing", "PENDING_REVIEW");

        mockMvc.perform(get("/api/v1/me/properties")
                        .header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + firstPropertyId + ")]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.id == " + secondPropertyId + ")]", hasSize(0)));
    }

    @Test
    void publicListStillWorksAfterManagementChanges() throws Exception {
        mockMvc.perform(get("/api/v1/properties").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(20)))
                .andExpect(jsonPath("$.pagination.total").value(20));
    }

    private Long createProperty(String token, String title, String lifecycleStatus) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(propertyRequest(title, lifecycleStatus))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String registerAndGetToken() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "agency" + suffix,
                                "email", "agency" + suffix + "@example.com",
                                "password", "Password123!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private Long createAgent(String token, String fullName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/me/agents")
                        .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", fullName,
                                "organization", "Integraciona agencija",
                                "email", UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "@agents.example.com",
                                "phone", "+381111111",
                                "image", "images/agents/test.jpg"))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Map<String, Object> propertyRequest(String title, String lifecycleStatus) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("lifecycleStatus", lifecycleStatus);
        request.put("basic", Map.of(
                "title", title,
                "description", "Integracioni test oglas",
                "propertyType", Map.of("id", 3, "name", "Stan"),
                "propertyStatus", List.of(Map.of("id", 1, "name", "Na prodaju")),
                "priceDollar", Map.of("sale", 250000)));
        request.put("address", Map.of(
                "formattedAddress", "Bulevar oslobodjenja 101, Novi Sad, Srbija",
                "city", Map.of("id", 1, "name", "Novi Sad"),
                "zipCode", "21000",
                "neighborhood", List.of(Map.of("id", 11, "name", "Grbavica")),
                "street", List.of(Map.of("id", 11, "name", "Bulevar oslobodjenja")),
                "lat", 45.255133,
                "lng", 19.845176));
        request.put("additional", Map.of(
                "bedrooms", 2,
                "bathrooms", 1,
                "garages", 0,
                "area", 900,
                "yearBuilt", 2018,
                "features", List.of(
                        Map.of("id", 1, "name", "Klima uredjaj", "selected", true),
                        Map.of("id", 2, "name", "Rostilj", "selected", false))));
        request.put("media", Map.of(
                "featured", false,
                "gallery", List.of(Map.of("link", "images/props/test/1.jpg")),
                "videos", List.of(),
                "plans", List.of(),
                "additionalFeatures", List.of()));
        return request;
    }
}
