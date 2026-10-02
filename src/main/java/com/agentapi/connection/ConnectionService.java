package com.agentapi.connection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.auth.CurrentUser;
import com.agentapi.connection.persistence.AiConnectionEntity;
import com.agentapi.connection.persistence.AiConnectionRepository;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.gateway.sidecar.CursorSidecarClient;
import com.agentapi.gateway.crypto.SecretEncryptionService;
import com.agentapi.web.ConnectionResponse;
import com.agentapi.web.ConnectionTestResponse;
import com.agentapi.web.CreateCursorConnectionRequest;

@Service
public class ConnectionService {

    public static final String PROVIDER_CURSOR = "CURSOR";

    private final AiConnectionRepository repository;
    private final SecretEncryptionService encryptionService;
    private final CurrentUser currentUser;
    private final CursorSidecarClient sidecarClient;

    public ConnectionService(
            AiConnectionRepository repository,
            SecretEncryptionService encryptionService,
            CurrentUser currentUser,
            CursorSidecarClient sidecarClient) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.currentUser = currentUser;
        this.sidecarClient = sidecarClient;
    }

    @Transactional(readOnly = true)
    public List<ConnectionResponse> listMine() {
        UUID userId = currentUser.requireUserId();
        return repository.findByIdUsuarioOrderByCriadoEmDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConnectionResponse createCursor(CreateCursorConnectionRequest request) {
        UUID userId = currentUser.requireUserId();
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Informe a API key da Cursor.");
        }
        AiConnectionEntity entity = new AiConnectionEntity();
        entity.setIdConnection(UUID.randomUUID());
        entity.setIdUsuario(userId);
        entity.setProvider(PROVIDER_CURSOR);
        entity.setLabel(request.getLabel() != null ? request.getLabel().trim() : "Cursor");
        entity.setApiKeyCiphertext(encryptionService.encrypt(request.getApiKey().trim()));
        entity.setActive(true);
        entity.setAtualizadoEm(LocalDateTime.now());
        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public ConnectionTestResponse test(UUID connectionId) {
        AiConnectionEntity entity = requireOwned(connectionId);
        String apiKey = encryptionService.decrypt(entity.getApiKeyCiphertext());
        boolean ok = sidecarClient.testApiKey(apiKey);
        if (ok) {
            entity.setLastValidatedAt(LocalDateTime.now());
            entity.setAtualizadoEm(LocalDateTime.now());
            repository.save(entity);
        }
        return new ConnectionTestResponse(ok, ok ? "Conexão válida." : "Não foi possível validar a API key.");
    }

    @Transactional
    public void delete(UUID connectionId) {
        AiConnectionEntity entity = requireOwned(connectionId);
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public AiConnectionEntity requireActiveConnection(UUID userId, UUID connectionId) {
        AiConnectionEntity entity = repository.findByIdConnectionAndIdUsuario(connectionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONNECTION_NOT_FOUND, "Conexão não encontrada."));
        if (!entity.isActive()) {
            throw new ApiException(ErrorCode.CONNECTION_REQUIRED, "Conexão Cursor inativa.");
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public String decryptApiKey(AiConnectionEntity entity) {
        return encryptionService.decrypt(entity.getApiKeyCiphertext());
    }

    @Transactional(readOnly = true)
    public AiConnectionEntity resolveConnectionForUser(UUID userId, UUID connectionId) {
        if (connectionId != null) {
            return requireActiveConnection(userId, connectionId);
        }
        return repository.findFirstByIdUsuarioAndActiveTrueOrderByAtualizadoEmDesc(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.CONNECTION_REQUIRED,
                        "Conecte sua conta Cursor antes de usar o chat."));
    }

    private AiConnectionEntity requireOwned(UUID connectionId) {
        UUID userId = currentUser.requireUserId();
        return repository.findByIdConnectionAndIdUsuario(connectionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.CONNECTION_NOT_FOUND, "Conexão não encontrada."));
    }

    private ConnectionResponse toResponse(AiConnectionEntity entity) {
        return new ConnectionResponse(
                entity.getIdConnection().toString(),
                entity.getProvider(),
                entity.getLabel(),
                entity.isActive(),
                entity.getLastValidatedAt() != null ? entity.getLastValidatedAt().toString() : null);
    }
}
