package com.housekey.agents.application;

import java.util.List;

import com.housekey.agents.api.AgentDtos.AgentCreateRequest;
import com.housekey.agents.api.AgentDtos.AgentResponse;
import com.housekey.agents.api.AgentDtos.AgentUpdateRequest;
import com.housekey.agents.infrastructure.AgentEntity;
import com.housekey.agents.infrastructure.AgentRepository;
import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.shared.error.LocalizedAccessDeniedException;
import com.housekey.shared.error.ResourceNotFoundException;
import com.housekey.users.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgentService {

    private final AgentRepository repository;

    public AgentService(AgentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getPublicAgents() {
        return repository.findByActiveTrueOrderByFullNameAscIdAsc()
                .stream()
                .map(AgentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgentResponse getPublicAgent(Long id) {
        return AgentResponse.from(repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.agent.notFound", id)));
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getMyAgents(AuthenticatedUser principal) {
        assertAgency(principal);
        return repository.findByAgencyUserIdAndActiveTrueOrderByFullNameAscIdAsc(principal.id())
                .stream()
                .map(AgentResponse::from)
                .toList();
    }

    public AgentResponse create(AgentCreateRequest request, AuthenticatedUser principal) {
        assertAgency(principal);
        AgentEntity entity = new AgentEntity(
                principal.id(),
                trimToNull(request.fullName()),
                trimToNull(request.organization()),
                trimToNull(request.email()),
                trimToNull(request.phone()),
                trimToNull(request.image()));
        return AgentResponse.from(repository.save(entity));
    }

    public AgentResponse update(Long id, AgentUpdateRequest request, AuthenticatedUser principal) {
        assertAgency(principal);
        AgentEntity entity = findOwnedAgent(id, principal);
        entity.update(
                trimToNull(request.fullName()),
                trimToNull(request.organization()),
                trimToNull(request.email()),
                trimToNull(request.phone()),
                trimToNull(request.image()),
                request.active() == null || request.active());
        return AgentResponse.from(repository.save(entity));
    }

    public void deactivate(Long id, AuthenticatedUser principal) {
        assertAgency(principal);
        AgentEntity entity = findOwnedAgent(id, principal);
        entity.deactivate();
        repository.save(entity);
    }

    private AgentEntity findOwnedAgent(Long id, AuthenticatedUser principal) {
        return repository.findByIdAndAgencyUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("error.agent.notFound", id));
    }

    private void assertAgency(AuthenticatedUser principal) {
        if (principal != null && principal.role() == UserRole.AGENCY) {
            return;
        }
        throw new LocalizedAccessDeniedException("error.accessDenied.manageAgents");
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
