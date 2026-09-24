package com.agentapi.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.AssistantService;
import com.agentapi.config.LlmProperties;
import com.agentapi.config.OllamaProperties;
import com.agentapi.llm.OllamaProbe;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.AssistantStatusDto;
import com.agentapi.web.LlmStatusDto;
import com.agentapi.web.StatusCheck;

@Service
public class AgentStatusService {

    private final AssistantService assistantService;
    private final LlmProperties llmProperties;
    private final OllamaProperties ollamaProperties;
    private final OllamaProbe ollamaProbe;

    public AgentStatusService(
            AssistantService assistantService,
            LlmProperties llmProperties,
            OllamaProperties ollamaProperties,
            OllamaProbe ollamaProbe) {
        this.assistantService = assistantService;
        this.llmProperties = llmProperties;
        this.ollamaProperties = ollamaProperties;
        this.ollamaProbe = ollamaProbe;
    }

    public AgentPlatformStatusResponse getStatus(String assistantParam) {
        List<StatusCheck> checks = new ArrayList<>();
        checks.add(new StatusCheck(
                "api",
                "OK",
                "Agent API em execução.",
                null));

        Optional<List<String>> modelNames = ollamaProbe.fetchModelNames();
        boolean llmReachable = modelNames.isPresent();
        String configuredModel = ollamaProperties.getModel();
        boolean modelReady = llmReachable
                && OllamaProbe.isModelAvailable(configuredModel, modelNames.orElse(List.of()));

        if (!llmReachable) {
            checks.add(new StatusCheck(
                    "llm-reachable",
                    "ERROR",
                    "Não foi possível conectar ao Ollama em " + ollamaProperties.getBaseUrl() + ".",
                    "Inicie o Ollama (app ou `ollama serve`) e confira OLLAMA_BASE_URL no .env da API."));
        } else {
            checks.add(new StatusCheck(
                    "llm-reachable",
                    "OK",
                    "Ollama acessível em " + ollamaProperties.getBaseUrl() + ".",
                    null));
        }

        if (llmReachable && !modelReady) {
            checks.add(new StatusCheck(
                    "llm-model",
                    "ERROR",
                    "Modelo \"" + configuredModel + "\" não encontrado no Ollama.",
                    "Execute: ollama pull " + configuredModel));
        } else if (llmReachable) {
            checks.add(new StatusCheck(
                    "llm-model",
                    "OK",
                    "Modelo \"" + configuredModel + "\" disponível no Ollama.",
                    null));
        }

        if (!"ollama".equalsIgnoreCase(llmProperties.getProvider())) {
            checks.add(new StatusCheck(
                    "llm-provider",
                    "WARN",
                    "Provedor LLM configurado: " + llmProperties.getProvider() + ".",
                    "Este MVP valida apenas integração com Ollama (llm.provider=ollama)."));
        }

        String activeAssistantId = resolveActiveAssistantId(assistantParam, checks);
        List<AssistantStatusDto> assistants = buildAssistantList();

        if (activeAssistantId != null) {
            boolean known = assistants.stream().anyMatch(a -> a.id().equals(activeAssistantId) && a.available());
            if (!known) {
                checks.add(new StatusCheck(
                        "assistant",
                        "ERROR",
                        "Assistente \"" + assistantParam + "\" não está disponível na API.",
                        "Use um dos IDs retornados em assistants (ex.: HORAS_EXTRAS)."));
            } else {
                checks.add(new StatusCheck(
                        "assistant",
                        "OK",
                        "Assistente ativo: " + activeAssistantId + ".",
                        null));
            }
        }

        boolean ready = checks.stream().noneMatch(check -> "ERROR".equals(check.level()));
        LlmStatusDto llm = new LlmStatusDto(
                llmProperties.getProvider(),
                ollamaProperties.getBaseUrl(),
                configuredModel,
                llmReachable,
                modelReady);

        return new AgentPlatformStatusResponse("UP", ready, llm, activeAssistantId, assistants, checks);
    }

    private List<AssistantStatusDto> buildAssistantList() {
        List<AssistantStatusDto> result = new ArrayList<>();
        for (Assistant assistant : assistantService.listActive()) {
            String model = assistant.resolveModel(ollamaProperties.getModel());
            result.add(new AssistantStatusDto(
                    assistant.code(),
                    assistant.name(),
                    assistant.description(),
                    model,
                    true));
        }
        return result;
    }

    private String resolveActiveAssistantId(String assistantParam, List<StatusCheck> checks) {
        if (assistantParam == null || assistantParam.isBlank()) {
            return null;
        }
        String normalized = AssistantCodes.normalize(assistantParam);
        if (assistantService.findActiveByCode(normalized).isPresent()) {
            return normalized;
        }
        return normalized;
    }
}
