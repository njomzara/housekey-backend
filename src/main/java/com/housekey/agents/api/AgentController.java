package com.housekey.agents.api;

import java.util.List;

import com.housekey.agents.api.AgentDtos.AgentCreateRequest;
import com.housekey.agents.api.AgentDtos.AgentResponse;
import com.housekey.agents.api.AgentDtos.AgentUpdateRequest;
import com.housekey.agents.application.AgentService;
import com.housekey.auth.domain.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/agents")
    @Operation(summary = "Get active public agent profiles")
    public List<AgentResponse> publicAgents() {
        return service.getPublicAgents();
    }

    @GetMapping("/api/v1/agents/{id}")
    @Operation(summary = "Get active public agent profile")
    public AgentResponse publicAgent(@PathVariable Long id) {
        return service.getPublicAgent(id);
    }

    @GetMapping("/api/v1/me/agents")
    @Operation(summary = "Get agents owned by the current agency")
    public List<AgentResponse> mine(@AuthenticationPrincipal AuthenticatedUser principal) {
        return service.getMyAgents(principal);
    }

    @PostMapping("/api/v1/me/agents")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an agent profile owned by the current agency")
    public AgentResponse create(
            @Valid @RequestBody AgentCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.create(request, principal);
    }

    @PutMapping("/api/v1/me/agents/{id}")
    @Operation(summary = "Update an agent profile owned by the current agency")
    public AgentResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AgentUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return service.update(id, request, principal);
    }

    @DeleteMapping("/api/v1/me/agents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate an agent profile owned by the current agency")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        service.deactivate(id, principal);
    }
}
