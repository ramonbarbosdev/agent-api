package com.agentapi.gateway.session;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.config.GatewayProperties;
import com.agentapi.connection.ConnectionService;
import com.agentapi.connection.persistence.AiConnectionEntity;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.gateway.project.PathAuthorizationService;
import com.agentapi.gateway.sidecar.CursorSidecarClient;

@Service
public class SessionManager {

    private final GatewaySessionRepository sessionRepository;
    private final GatewayProperties gatewayProperties;
    private final PathAuthorizationService pathAuthorizationService;
    private final ConnectionService connectionService;
    private final CursorSidecarClient sidecarClient;

    public SessionManager(
            GatewaySessionRepository sessionRepository,
            GatewayProperties gatewayProperties,
            PathAuthorizationService pathAuthorizationService,
            ConnectionService connectionService,
            CursorSidecarClient sidecarClient) {
        this.sessionRepository = sessionRepository;
        this.gatewayProperties = gatewayProperties;
        this.pathAuthorizationService = pathAuthorizationService;
        this.connectionService = connectionService;
        this.sidecarClient = sidecarClient;
    }

    @Transactional
    public GatewaySessionEntity getOrCreateForChat(
            UUID userId,
            AssistenteEntity assistente,
            UUID conversationId) {
        return sessionRepository.findByIdConversa(conversationId).orElseGet(() -> {
            enforceSessionLimit(userId);
            AiConnectionEntity connection = connectionService.resolveConnectionForUser(
                    userId, assistente.getIdConnection());
            Path projectPath = pathAuthorizationService.resolveWorkspacePath(assistente.getCdProjeto());
            String apiKey = connectionService.decryptApiKey(connection);

            GatewaySessionEntity session = new GatewaySessionEntity();
            session.setIdSession(UUID.randomUUID());
            session.setIdUsuario(userId);
            session.setIdAssistente(assistente.getIdAssistente());
            session.setIdConnection(connection.getIdConnection());
            session.setIdConversa(conversationId);
            session.setStatus(GatewaySessionStatus.CREATED);
            sessionRepository.save(session);

            String cursorAgentId = sidecarClient.createSession(
                    session.getIdSession().toString(),
                    apiKey,
                    projectPath.toString(),
                    assistente.getDsSystemPrompt());
            session.setCursorAgentId(cursorAgentId);
            session.setStatus(GatewaySessionStatus.RUNNING);
            session.setAtualizadoEm(LocalDateTime.now());
            return sessionRepository.save(session);
        });
    }

    @Transactional
    public void markUpdated(GatewaySessionEntity session, String status) {
        session.setStatus(status);
        session.setAtualizadoEm(LocalDateTime.now());
        sessionRepository.save(session);
    }

    private void enforceSessionLimit(UUID userId) {
        long active = sessionRepository.countByIdUsuarioAndStatusIn(
                userId,
                List.of(GatewaySessionStatus.CREATED, GatewaySessionStatus.RUNNING, GatewaySessionStatus.WAITING));
        if (active >= gatewayProperties.getMaxSessions()) {
            throw new ApiException(
                    ErrorCode.INVALID_REQUEST,
                    "Limite de sessões simultâneas atingido.");
        }
    }
}
