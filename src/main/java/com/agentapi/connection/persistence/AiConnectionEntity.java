package com.agentapi.connection.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_connection", schema = "agent")
public class AiConnectionEntity {

    @Id
    @Column(name = "id_connection", nullable = false)
    private UUID idConnection;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "tp_provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "nm_label", length = 128)
    private String label;

    @Column(name = "ds_api_key_ciphertext", nullable = false, columnDefinition = "TEXT")
    private String apiKeyCiphertext;

    @Column(name = "fl_ativo", nullable = false)
    private boolean active = true;

    @Column(name = "dt_last_validated")
    private LocalDateTime lastValidatedAt;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "dt_atualizacao", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    public UUID getIdConnection() {
        return idConnection;
    }

    public void setIdConnection(UUID idConnection) {
        this.idConnection = idConnection;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getApiKeyCiphertext() {
        return apiKeyCiphertext;
    }

    public void setApiKeyCiphertext(String apiKeyCiphertext) {
        this.apiKeyCiphertext = apiKeyCiphertext;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastValidatedAt() {
        return lastValidatedAt;
    }

    public void setLastValidatedAt(LocalDateTime lastValidatedAt) {
        this.lastValidatedAt = lastValidatedAt;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
