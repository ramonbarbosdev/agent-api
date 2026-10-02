package com.agentapi.agent;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.auth.CurrentUser;
import com.agentapi.config.GatewayProperties;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.gateway.audit.GatewayAuditService;
import com.agentapi.gateway.session.GatewaySessionEntity;
import com.agentapi.gateway.session.GatewaySessionStatus;
import com.agentapi.gateway.session.SessionManager;
import com.agentapi.gateway.sidecar.CursorSidecarClient;
import com.agentapi.web.AgentChatHistoryMessage;

@Component
public class CursorAgentRuntime {

    private final CurrentUser currentUser;
    private final AssistenteRepository assistenteRepository;
    private final SessionManager sessionManager;
    private final CursorSidecarClient sidecarClient;
    private final GatewayAuditService auditService;
    private final GatewayProperties gatewayProperties;

    public CursorAgentRuntime(
            CurrentUser currentUser,
            AssistenteRepository assistenteRepository,
            SessionManager sessionManager,
            CursorSidecarClient sidecarClient,
            GatewayAuditService auditService,
            GatewayProperties gatewayProperties) {
        this.currentUser = currentUser;
        this.assistenteRepository = assistenteRepository;
        this.sessionManager = sessionManager;
        this.sidecarClient = sidecarClient;
        this.auditService = auditService;
        this.gatewayProperties = gatewayProperties;
    }

    public String run(AgentContext context, String userMessage, List<AgentChatHistoryMessage> history) {
        return runStream(context, userMessage, history, null);
    }

    public String runStream(
            AgentContext context,
            String userMessage,
            List<AgentChatHistoryMessage> history,
            AgentStreamEmitter emitter) {
        UUID userId = context.getUserId() != null ? context.getUserId() : currentUser.requireUserId();
        validateMessage(userMessage);
        AssistenteEntity assistente = requireOwnedAssistant(userId, context.getAssistantCode());
        GatewaySessionEntity session = sessionManager.getOrCreateForChat(
                userId, assistente, context.getConversationId());

        StringBuilder full = new StringBuilder();
        if (emitter != null) {
            emitter.onPhase("Cursor processando…");
        }
        try {
            sidecarClient.sendMessage(session.getIdSession().toString(), userMessage, chunk -> {
                full.append(chunk);
                if (emitter != null) {
                    emitter.onToken(chunk);
                }
            });
            sessionManager.markUpdated(session, GatewaySessionStatus.COMPLETED);
            auditService.record(userId, session.getIdSession(), assistente.getCdProjeto(), assistente.getCdAssistente(), "MESSAGE", "SUCCESS");
            return full.toString();
        } catch (ApiException ex) {
            sessionManager.markUpdated(session, GatewaySessionStatus.FAILED);
            auditService.record(userId, session.getIdSession(), assistente.getCdProjeto(), assistente.getCdAssistente(), "MESSAGE", "FAILED");
            throw ex;
        }
    }

    private AssistenteEntity requireOwnedAssistant(UUID userId, String code) {
        return assistenteRepository.findByCdAssistenteIgnoreCaseAndIdUsuario(code, userId)
                .filter(AssistenteEntity::isFlAtivo)
                .orElseThrow(() -> new AssistantNotFoundException(code));
    }

    private void validateMessage(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Mensagem vazia.");
        }
        if (userMessage.length() > gatewayProperties.getMaxMessageChars()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Mensagem excede o tamanho máximo.");
        }
    }
}
