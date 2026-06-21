package com.housekey.agents.infrastructure;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "agents")
public class AgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "agent_id_generator")
    @SequenceGenerator(name = "agent_id_generator", sequenceName = "agents_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "agency_user_id")
    private Long agencyUserId;

    @Column(name = "full_name")
    private String fullName;

    private String organization;

    private String email;

    private String phone;

    private String image;

    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected AgentEntity() {
    }

    public AgentEntity(
            Long agencyUserId,
            String fullName,
            String organization,
            String email,
            String phone,
            String image) {
        this.agencyUserId = agencyUserId;
        this.fullName = fullName;
        this.organization = organization;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.active = true;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getAgencyUserId() {
        return agencyUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String fullName,
            String organization,
            String email,
            String phone,
            String image,
            boolean active) {
        this.fullName = fullName;
        this.organization = organization;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }
}
