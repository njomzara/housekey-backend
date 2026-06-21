package com.housekey.agents.domain;

public record Agent(
        Long id,
        Long agencyUserId,
        String fullName,
        String organization,
        String email,
        String phone,
        String image,
        boolean active) {
}
