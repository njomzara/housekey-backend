package com.housekey.agents.api;

import java.time.LocalDateTime;

import com.housekey.agents.infrastructure.AgentEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AgentDtos {

    private AgentDtos() {
    }

    public record AgentResponse(
            Long id,
            Long agencyUserId,
            String fullName,
            String organization,
            String email,
            String phone,
            String image,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        public static AgentResponse from(AgentEntity entity) {
            return new AgentResponse(
                    entity.getId(),
                    entity.getAgencyUserId(),
                    entity.getFullName(),
                    entity.getOrganization(),
                    entity.getEmail(),
                    entity.getPhone(),
                    entity.getImage(),
                    entity.isActive(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt());
        }
    }

    public record AgentCreateRequest(
            @NotBlank @Size(max = 160) String fullName,
            @Size(max = 160) String organization,
            @Email @Size(max = 160) String email,
            @Size(max = 80) String phone,
            @Size(max = 255) String image) {
    }

    public record AgentUpdateRequest(
            @NotBlank @Size(max = 160) String fullName,
            @Size(max = 160) String organization,
            @Email @Size(max = 160) String email,
            @Size(max = 80) String phone,
            @Size(max = 255) String image,
            Boolean active) {
    }
}
