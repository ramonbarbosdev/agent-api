package com.agentapi.gateway.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.auth.CurrentUser;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.gateway.session.GatewaySessionEntity;
import com.agentapi.gateway.session.GatewaySessionRepository;
import com.agentapi.web.GatewaySessionResponse;

@RestController
@RequestMapping("/api/sessions")
public class GatewaySessionController {

    private final GatewaySessionRepository sessionRepository;
    private final CurrentUser currentUser;

    public GatewaySessionController(GatewaySessionRepository sessionRepository, CurrentUser currentUser) {
        this.sessionRepository = sessionRepository;
        this.currentUser = currentUser;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GatewaySessionResponse> list() {
        UUID userId = currentUser.requireUserId();
        return sessionRepository.findAll().stream()
                .filter(s -> s.getIdUsuario().equals(userId))
                .map(GatewaySessionController::toResponse)
                .toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GatewaySessionResponse get(@PathVariable UUID id) {
        return toResponse(requireOwned(id));
    }

    private GatewaySessionEntity requireOwned(UUID id) {
        UUID userId = currentUser.requireUserId();
        GatewaySessionEntity entity = sessionRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.SESSION_NOT_FOUND, "Sessão não encontrada."));
        if (!entity.getIdUsuario().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Sessão não pertence ao usuário.");
        }
        return entity;
    }

    private static GatewaySessionResponse toResponse(GatewaySessionEntity entity) {
        return new GatewaySessionResponse(
                entity.getIdSession().toString(),
                entity.getStatus(),
                entity.getIdConversa() != null ? entity.getIdConversa().toString() : null,
                entity.getCursorAgentId());
    }
}
