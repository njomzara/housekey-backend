package com.housekey.auth.api;

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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerSucceeds() throws Exception {
        String suffix = uniqueSuffix();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "agency" + suffix,
                                "email", "agency" + suffix + "@example.com",
                                "password", "Password123!",
                                "firstName", "Test",
                                "lastName", "Agency"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("agency" + suffix))
                .andExpect(jsonPath("$.user.email").value("agency" + suffix + "@example.com"))
                .andExpect(jsonPath("$.user.role").value("AGENCY"))
                .andExpect(jsonPath("$.user.roles[0]").value("AGENCY"));
    }

    @Test
    void duplicateUsernameAndEmailFails() throws Exception {
        String suffix = uniqueSuffix();
        Map<String, Object> request = Map.of(
                "username", "agency" + suffix,
                "email", "agency" + suffix + "@example.com",
                "password", "Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors.username").value("Korisnicko ime je vec registrovano."))
                .andExpect(jsonPath("$.fieldErrors.email").value("Email je vec registrovan."));
    }

    @Test
    void loginSucceedsWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "agency.demo",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.user.username").value("agency.demo"))
                .andExpect(jsonPath("$.user.role").value("AGENCY"));
    }

    @Test
    void loginAllowsLocalDevOriginOnDynamicPort() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Origin", "http://127.0.0.1:4311")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "agency.demo",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:4311"))
                .andExpect(jsonPath("$.accessToken", not(blankOrNullString())));
    }

    @Test
    void loginFailsWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "agency.demo",
                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Neispravno korisnicko ime/email ili lozinka."));
    }

    @Test
    void loginFailsWithInvalidCredentialsInEnglishWhenRequested() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "agency.demo",
                                "password", "wrong-password"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password."));
    }

    @Test
    void usersMeRequiresToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Prijava je potrebna."));
    }

    @Test
    void usersMeReturnsCurrentUserWithToken() throws Exception {
        String token = loginAsDemoAgency();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("agency.demo"))
                .andExpect(jsonPath("$.email").value("agency.demo@housekey.local"))
                .andExpect(jsonPath("$.role").value("AGENCY"))
                .andExpect(jsonPath("$.roles[0]").value("AGENCY"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void publicPropertyEndpointsStillWorkWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/properties").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    private String loginAsDemoAgency() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "usernameOrEmail", "agency.demo",
                                "password", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
