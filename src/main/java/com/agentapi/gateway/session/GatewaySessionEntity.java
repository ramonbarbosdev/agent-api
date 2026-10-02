package com.agentapi.gateway.session;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "gateway_session", schema = "agent")
public class GatewaySessionEntity {

    @Id
    @Column(name = "id_session", nullable = false)
    private UUID idSession;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_assistente", nullable = false)
    private UUID idAssistente;

    @Column(name = "id_connection", nullable = false)
    private UUID idConnection;

    @Column(name = "id_conversa")
    private UUID idConversa;

    @Column(name = "cd_cursor_agent_id")
    private String cursorAgentId;

    @Column(name = "tp_status", nullable = false, length = 32)
    private String status;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "dt_atualizacao", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    public UUID getIdSession() {
        return idSession;
    }

    public void setIdSession(UUID idSession) {
        this.idSession = idSession;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public UUID getIdAssistente() {
        return idAssistente;
    }

    public void setIdAssistente(UUID idAssistente) {
        this.idAssistente = idAssistente;
    }

    public UUID getIdConnection() {
        return idConnection;
    }

    public void setIdConnection(UUID idConnection) {
        this.idConnection = idConnection;
    }

    public UUID getIdConversa() {
        return idConversa;
    }

    public void setIdConversa(UUID idConversa) {
        this.idConversa = idConversa;
    }

    public String getCursorAgentId() {
        return cursorAgentId;
    }

    public void setCursorAgentId(String cursorAgentId) {
        this.cursorAgentId = cursorAgentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
