package com.agentapi.conversation.persistence;

import java.util.UUID;

import com.agentapi.model.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversa", schema = "agent")
public class ConversaEntity extends AuditableEntity {

    @Id
    @Column(name = "id_conversa", nullable = false)
    private UUID idConversa;

    @Column(name = "id_usuario")
    private UUID idUsuario;

    @Column(name = "nm_assistente", nullable = false, length = 64)
    private String nmAssistente;

    protected ConversaEntity() {
    }

    public ConversaEntity(UUID idConversa, String nmAssistente, UUID idUsuario) {
        this.idConversa = idConversa;
        this.nmAssistente = nmAssistente;
        this.idUsuario = idUsuario;
    }

    public UUID getIdConversa() {
        return idConversa;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public String getNmAssistente() {
        return nmAssistente;
    }

    public void touch() {
        onUpdate();
    }
}
