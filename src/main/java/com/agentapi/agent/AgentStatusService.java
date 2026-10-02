package com.agentapi.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.AssistantService;
import com.agentapi.auth.CurrentUser;
import com.agentapi.connection.persistence.AiConnectionRepository;
import com.agentapi.gateway.sidecar.CursorSidecarClient;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.AssistantStatusDto;
import com.agentapi.web.CursorStatusDto;
import com.agentapi.web.LlmStatusDto;
import com.agentapi.web.StatusCheck;

@Service
public class AgentStatusService {

    private final AssistantService assistantService;
    private final CurrentUser currentUser;
    private final AiConnectionRepository connectionRepository;
    private final CursorSidecarClient sidecarClient;

    public AgentStatusService(
            AssistantService assistantService,
            CurrentUser currentUser,
            AiConnectionRepository connectionRepository,
            CursorSidecarClient sidecarClient) {
        this.assistantService = assistantService;
        this.currentUser = currentUser;
        this.connectionRepository = connectionRepository;
        this.sidecarClient = sidecarClient;
    }

    public AgentPlatformStatusResponse getStatus(String assistantParam) {
        List<StatusCheck> checks = new ArrayList<>();
        checks.add(new StatusCheck("api", "OK", "Agent API em execução.", null));

        boolean sidecarOk = sidecarClient.isHealthy();
        checks.add(new StatusCheck(
                "sidecar",
                sidecarOk ? "OK" : "ERROR",
                sidecarOk ? "Sidecar Cursor acessível." : "Sidecar Cursor indisponível.",
                sidecarOk ? null : "Inicie cursor-sidecar no VPS."));

        boolean connected = false;
        boolean testOk = false;
        UUID userId = currentUser.userIdOrNull();
        if (userId != null) {
            var conn = connectionRepository.findFirstByIdUsuarioAndActiveTrueOrderByAtualizadoEmDesc(userId);
            connected = conn.isPresent();
            testOk = connected && conn.get().getLastValidatedAt() != null;
        }
        if (!connected) {
            checks.add(new StatusCheck(
                    "cursor-connection",
                    "WARN",
                    "Nenhuma conexão Cursor ativa.",
                    "Conecte sua API key em /api/connections/cursor."));
        } else {
            checks.add(new StatusCheck(
                    "cursor-connection",
                    testOk ? "OK" : "WARN",
                    testOk ? "Conexão Cursor validada." : "Conexão Cursor não testada.",
                    testOk ? null : "Execute POST /api/connections/{id}/test."));
        }

        LlmStatusDto llm = new LlmStatusDto("cursor", "", "", sidecarOk, connected);
        CursorStatusDto cursor = new CursorStatusDto(connected, testOk, sidecarOk);

        String activeAssistantId = resolveActiveAssistantId(assistantParam, checks);
        List<AssistantStatusDto> assistants = buildAssistantList();

        if (activeAssistantId != null) {
            boolean known = assistants.stream().anyMatch(a -> a.id().equals(activeAssistantId) && a.available());
            if (!known) {
                checks.add(new StatusCheck(
                        "assistant",
                        "ERROR",
                        "Assistente \"" + assistantParam + "\" não está disponível.",
                        "Crie um agente em /api/agent/assistants."));
            }
        }

        boolean ready = checks.stream().noneMatch(check -> "ERROR".equals(check.level()));
        return new AgentPlatformStatusResponse("UP", ready, llm, cursor, activeAssistantId, assistants, checks);
    }

    private List<AssistantStatusDto> buildAssistantList() {
        List<AssistantStatusDto> result = new ArrayList<>();
        List<Assistant> list = currentUser.userIdOrNull() != null
                ? assistantService.listActiveForCurrentUser()
                : List.of();
        for (Assistant assistant : list) {
            result.add(new AssistantStatusDto(
                    assistant.code(),
                    assistant.name(),
                    assistant.description(),
                    "cursor",
                    true));
        }
        return result;
    }

    private String resolveActiveAssistantId(String assistantParam, List<StatusCheck> checks) {
        if (assistantParam == null || assistantParam.isBlank()) {
            return null;
        }
        String normalized = AssistantCodes.normalize(assistantParam);
        if (currentUser.userIdOrNull() != null) {
            try {
                assistantService.requireActiveByCodeForUser(normalized, currentUser.requireUserId());
                return normalized;
            } catch (Exception ex) {
                return normalized;
            }
        }
        return normalized;
    }
}
