package com.agentapi.assistant;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.assistant.persistence.AssistenteToolEntity;
import com.agentapi.assistant.persistence.AssistenteToolRepository;
import com.agentapi.auth.CurrentUser;
import com.agentapi.connection.ConnectionService;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.gateway.project.PathAuthorizationService;
import com.agentapi.tool.ToolRegistry;
import com.agentapi.web.AssistantRequest;
import com.agentapi.web.AssistantResponse;

@Service
public class AssistantConfigService {

    private static final String BASE_PROMPT_PATH = "prompts/base-system.txt";

    private final AssistenteRepository assistenteRepository;
    private final AssistenteToolRepository assistenteToolRepository;
    private final AssistantMapper assistantMapper;
    private final PromptLoader promptLoader;
    private final PromptComposer promptComposer;
    private final ToolRegistry toolRegistry;
    private final CurrentUser currentUser;
    private final PathAuthorizationService pathAuthorizationService;
    private final ConnectionService connectionService;

    public AssistantConfigService(
            AssistenteRepository assistenteRepository,
            AssistenteToolRepository assistenteToolRepository,
            AssistantMapper assistantMapper,
            PromptLoader promptLoader,
            PromptComposer promptComposer,
            ToolRegistry toolRegistry,
            CurrentUser currentUser,
            PathAuthorizationService pathAuthorizationService,
            ConnectionService connectionService) {
        this.assistenteRepository = assistenteRepository;
        this.assistenteToolRepository = assistenteToolRepository;
        this.assistantMapper = assistantMapper;
        this.promptLoader = promptLoader;
        this.promptComposer = promptComposer;
        this.toolRegistry = toolRegistry;
        this.currentUser = currentUser;
        this.pathAuthorizationService = pathAuthorizationService;
        this.connectionService = connectionService;
    }

    @Transactional(readOnly = true)
    public List<AssistantResponse> listResponses(boolean includeInactive) {
        UUID userId = currentUser.requireUserId();
        List<AssistenteEntity> entities = includeInactive
                ? assistenteRepository.findByIdUsuarioOrderByNmNomeAsc(userId)
                : assistenteRepository.findByIdUsuarioAndFlAtivoTrueOrderByNmNomeAsc(userId);
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssistantResponse getByCode(String code) {
        return toResponse(requireEntity(code));
    }

    @Transactional
    public AssistantResponse create(AssistantRequest request) {
        UUID userId = currentUser.requireUserId();
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Informe o código (code) do assistente.");
        }
        String code = AssistantCodes.normalize(request.getCode());
        validateCode(code);
        if (assistenteRepository.existsByCdAssistenteIgnoreCaseAndIdUsuario(code, userId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Já existe assistente com código: " + code);
        }
        validateTools(request.getTools());
        validateCursorFields(request, userId);

        AssistenteEntity entity = new AssistenteEntity();
        entity.setIdAssistente(UUID.randomUUID());
        entity.setIdUsuario(userId);
        applyRequest(entity, request, code, true);
        assistenteRepository.save(entity);
        replaceTools(entity.getIdAssistente(), request.getTools());
        return toResponse(entity);
    }

    @Transactional
    public AssistantResponse update(String code, AssistantRequest request) {
        AssistenteEntity entity = requireEntity(code);
        validateTools(request.getTools());
        validateCursorFields(request, entity.getIdUsuario());
        applyRequest(entity, request, entity.getCdAssistente(), false);
        assistenteRepository.save(entity);
        replaceTools(entity.getIdAssistente(), request.getTools());
        return toResponse(entity);
    }

    @Transactional
    public void delete(String code) {
        AssistenteEntity entity = requireEntity(code);
        assistenteRepository.delete(entity);
    }

    private void validateCursorFields(AssistantRequest request, UUID userId) {
        pathAuthorizationService.ensureWorkspaceConfigured();
        if (request.getProjectId() != null && !request.getProjectId().isBlank()) {
            pathAuthorizationService.resolveWorkspacePath(request.getProjectId().trim());
        }
        UUID connectionId = parseConnectionId(request.getConnectionId());
        connectionService.requireActiveConnection(userId, connectionId);
    }

    private static UUID parseConnectionId(String connectionId) {
        if (connectionId == null || connectionId.isBlank()) {
            throw new ApiException(ErrorCode.CONNECTION_REQUIRED, "Informe connectionId do agente.");
        }
        try {
            return UUID.fromString(connectionId);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "connectionId inválido.");
        }
    }

    private void applyRequest(AssistenteEntity entity, AssistantRequest request, String code, boolean isCreate) {
        if (isCreate) {
            entity.setCdAssistente(code);
        }
        entity.setNmNome(request.getName().trim());
        entity.setDsDescricao(trimToNull(request.getDescription()));
        entity.setDsSystemPrompt(resolveSystemPrompt(request));
        entity.setNmModelo(trimToNull(request.getModel()));
        entity.setFlAtivo(request.isActive());
        entity.setFlRagInject(request.isRagInject());
        entity.setNuRagTopK(Math.max(1, request.getRagTopK()));
        entity.setCdProjeto(trimToNull(request.getProjectId()));
        entity.setIdConnection(parseConnectionId(request.getConnectionId()));
    }

    private String resolveSystemPrompt(AssistantRequest request) {
        String domain = request.getSystemPrompt() != null ? request.getSystemPrompt().trim() : "";
        if (request.isPrependBasePrompt()) {
            String base = promptLoader.load(BASE_PROMPT_PATH);
            return promptComposer.compose(base, domain);
        }
        return domain;
    }

    private void replaceTools(UUID idAssistente, List<String> tools) {
        assistenteToolRepository.deleteByIdAssistente(idAssistente);
        if (tools == null) {
            return;
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String tool : tools) {
            if (tool != null && !tool.isBlank()) {
                unique.add(tool.trim());
            }
        }
        for (String toolName : unique) {
            assistenteToolRepository.save(new AssistenteToolEntity(
                    UUID.randomUUID(),
                    idAssistente,
                    toolName));
        }
    }

    private AssistantResponse toResponse(AssistenteEntity entity) {
        List<String> tools = assistenteToolRepository.findByIdAssistenteOrderByNmToolAsc(entity.getIdAssistente())
                .stream()
                .map(AssistenteToolEntity::getNmTool)
                .toList();
        Assistant domain = assistantMapper.toDomain(entity);
        return new AssistantResponse(
                domain.code(),
                domain.name(),
                domain.description(),
                domain.systemPrompt(),
                domain.modelOverride(),
                domain.active(),
                domain.ragInjectEnabled(),
                domain.ragTopK(),
                tools,
                entity.getCdProjeto(),
                entity.getIdConnection() != null ? entity.getIdConnection().toString() : null);
    }

    private AssistenteEntity requireEntity(String code) {
        UUID userId = currentUser.requireUserId();
        return assistenteRepository.findByCdAssistenteIgnoreCaseAndIdUsuario(AssistantCodes.normalize(code), userId)
                .orElseThrow(() -> new AssistantNotFoundException(code));
    }

    private static void validateCode(String code) {
        if (code.isBlank() || !code.matches("[A-Z0-9_]{2,64}")) {
            throw new ApiException(
                    ErrorCode.INVALID_REQUEST,
                    "Código inválido. Use 2–64 caracteres: letras, números e underscore.");
        }
    }

    private void validateTools(List<String> tools) {
        if (tools == null) {
            return;
        }
        for (String tool : tools) {
            if (tool == null || tool.isBlank()) {
                continue;
            }
            if (toolRegistry.getByName(tool.trim()).isEmpty()) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "Tool desconhecida: " + tool);
            }
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
