package com.agentapi.assistant.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assistente_tool", schema = "agent")
public class AssistenteToolEntity {

    @Id
    @Column(name = "id_assistentetool", nullable = false)
    private UUID idAssistenteTool;

    @Column(name = "id_assistente", nullable = false)
    private UUID idAssistente;

    @Column(name = "nm_tool", nullable = false, length = 128)
    private String nmTool;

    @Column(name = "dt_criacao", nullable = false)
    private Instant dtCriacao = Instant.now();

    protected AssistenteToolEntity() {
    }

    public AssistenteToolEntity(UUID idAssistenteTool, UUID idAssistente, String nmTool) {
        this.idAssistenteTool = idAssistenteTool;
        this.idAssistente = idAssistente;
        this.nmTool = nmTool;
    }

    public UUID getIdAssistenteTool() {
        return idAssistenteTool;
    }

    public UUID getIdAssistente() {
        return idAssistente;
    }

    public String getNmTool() {
        return nmTool;
    }
}
