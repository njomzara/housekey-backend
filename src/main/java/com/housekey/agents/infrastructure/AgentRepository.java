package com.housekey.agents.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<AgentEntity, Long> {

    List<AgentEntity> findByActiveTrueOrderByFullNameAscIdAsc();

    List<AgentEntity> findByAgencyUserIdAndActiveTrueOrderByFullNameAscIdAsc(Long agencyUserId);

    Optional<AgentEntity> findByIdAndActiveTrue(Long id);

    Optional<AgentEntity> findByIdAndAgencyUserId(Long id, Long agencyUserId);

    boolean existsByIdAndAgencyUserIdAndActiveTrue(Long id, Long agencyUserId);
}
