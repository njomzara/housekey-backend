package com.housekey.media.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PropertyMediaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ownerCanUploadImage() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Media owner upload listing", "PUBLISHED");

        mockMvc.perform(multipart("/api/v1/properties/{propertyId}/media/images", propertyId)
                        .file(imageFile("file", "living-room.jpg", 1200, 800))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", greaterThan(0)))
                .andExpect(jsonPath("$[0].mediaKind").value("IMAGE"))
                .andExpect(jsonPath("$[0].sortOrder").value(0))
                .andExpect(jsonPath("$[0].small", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$[0].medium", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$[0].big", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$[0].variants", hasSize(4)))
                .andExpect(jsonPath("$[0].variants[?(@.variantType == 'ORIGINAL')].width", hasItem(1200)))
                .andExpect(jsonPath("$[0].variants[?(@.variantType == 'ORIGINAL')].height", hasItem(800)));

        mockMvc.perform(get("/api/v1/properties/{propertyId}/media", propertyId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].variants", hasSize(4)));
    }

    @Test
    void nonOwnerCannotUpload() throws Exception {
        String ownerToken = registerAndGetToken();
        String otherToken = registerAndGetToken();
        Long propertyId = createProperty(ownerToken, "Protected media listing", "PUBLISHED");

        mockMvc.perform(multipart("/api/v1/properties/{propertyId}/media/images", propertyId)
                        .file(imageFile("file", "blocked.jpg", 800, 600))
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unsupportedFileTypeRejected() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Unsupported media listing", "PUBLISHED");
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "notes.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/properties/{propertyId}/media/images", propertyId)
                        .file(textFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['files[0]']").value("Nepodrzan tip slike."));
    }

    @Test
    void tooLargeFileRejected() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Too large media listing", "PUBLISHED");
        MockMultipartFile oversized = new MockMultipartFile(
                "file",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[1_048_577]);

        mockMvc.perform(multipart("/api/v1/properties/{propertyId}/media/images", propertyId)
                        .file(oversized)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['files[0]']").value("Slika prelazi maksimalnu dozvoljenu velicinu."));
    }

    @Test
    void propertyDetailAndListReturnUploadedGalleryVariants() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Public uploaded gallery listing", "PUBLISHED");

        mockMvc.perform(multipart("/api/v1/properties/{propertyId}/media/images", propertyId)
                        .file(imageFile("file", "front.jpg", 1200, 800))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/properties/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gallery", hasSize(1)))
                .andExpect(jsonPath("$.gallery[0].small", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$.gallery[0].medium", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$.gallery[0].big", startsWith("/uploads/properties/" + propertyId + "/media/")))
                .andExpect(jsonPath("$.gallery[0].mediaId").exists());

        mockMvc.perform(get("/api/v1/properties").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.data[?(@.id == " + propertyId + ")].gallery[0].small",
                        hasItem(startsWith("/uploads/properties/" + propertyId + "/media/"))));
    }

    @Test
    void deleteMediaRemovesItFromPublicPropertyResponse() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Delete media listing", "PUBLISHED");
        Long mediaId = uploadImage(token, propertyId, "delete-me.jpg", 1000, 700).get(0).get("id").asLong();

        mockMvc.perform(delete("/api/v1/properties/{propertyId}/media/{mediaId}", propertyId, mediaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/properties/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gallery[0].small").value("images/props/test/1.jpg"))
                .andExpect(jsonPath("$.gallery[0].medium").value("images/props/test/1.jpg"))
                .andExpect(jsonPath("$.gallery[0].big").value("images/props/test/1.jpg"));
    }

    @Test
    void orderingEndpointUpdatesGalleryOrder() throws Exception {
        String token = registerAndGetToken();
        Long propertyId = createProperty(token, "Order media listing", "PUBLISHED");
        JsonNode uploaded = uploadImages(
                token,
                propertyId,
                imageFile("files", "first.jpg", 1000, 700),
                imageFile("files", "second.jpg", 1000, 700));
        Long firstId = uploaded.get(0).get("id").asLong();
        Long secondId = uploaded.get(1).get("id").asLong();

        mockMvc.perform(put("/api/v1/properties/{propertyId}/media/order", propertyId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mediaIds", List.of(secondId, firstId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(secondId))
                .andExpect(jsonPath("$[0].sortOrder").value(0))
                .andExpect(jsonPath("$[1].id").value(firstId))
                .andExpect(jsonPath("$[1].sortOrder").value(1));

        mockMvc.perform(get("/api/v1/properties/{propertyId}", propertyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gallery[0].mediaId").value(secondId))
                .andExpect(jsonPath("$.gallery[1].mediaId").value(firstId));
    }

    private JsonNode uploadImage(String token, Long propertyId, String filename, int width, int height) throws Exception {
        return uploadImages(token, propertyId, imageFile("file", filename, width, height));
    }

    private JsonNode uploadImages(String token, Long propertyId, MockMultipartFile... files) throws Exception {
        MockMultipartHttpServletRequestBuilder builder =
                multipart("/api/v1/properties/{propertyId}/media/images", propertyId);
        builder.header("Authorization", "Bearer " + token);
        for (MockMultipartFile file : files) {
            builder.file(file);
        }

        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private MockMultipartFile imageFile(String partName, String filename, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(42, 99, 160));
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(new Color(240, 240, 240));
            graphics.fillRect(width / 6, height / 6, width / 2, height / 2);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return new MockMultipartFile(partName, filename, MediaType.IMAGE_JPEG_VALUE, output.toByteArray());
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
                                "username", "media" + suffix,
                                "email", "media" + suffix + "@example.com",
                                "password", "Password123!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
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
