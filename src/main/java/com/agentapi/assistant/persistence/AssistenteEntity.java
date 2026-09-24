package com.agentapi.assistant.persistence;

import java.util.UUID;

import com.agentapi.model.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assistente", schema = "agent")
public class AssistenteEntity extends AuditableEntity {

    @Id
    @Column(name = "id_assistente", nullable = false)
    private UUID idAssistente;

    @Column(name = "cd_assistente", nullable = false, length = 64)
    private String cdAssistente;

    @Column(name = "nm_nome", nullable = false)
    private String nmNome;

    @Column(name = "ds_descricao", length = 512)
    private String dsDescricao;

    @Column(name = "ds_system_prompt", nullable = false, columnDefinition = "TEXT")
    private String dsSystemPrompt;

    @Column(name = "nm_modelo", length = 128)
    private String nmModelo;

    @Column(name = "fl_ativo", nullable = false)
    private boolean flAtivo = true;

    @Column(name = "fl_rag_inject", nullable = false)
    private boolean flRagInject = true;

    @Column(name = "nu_rag_top_k", nullable = false)
    private int nuRagTopK = 4;

    public AssistenteEntity() {
    }

    public UUID getIdAssistente() {
        return idAssistente;
    }

    public void setIdAssistente(UUID idAssistente) {
        this.idAssistente = idAssistente;
    }

    public String getCdAssistente() {
        return cdAssistente;
    }

    public void setCdAssistente(String cdAssistente) {
        this.cdAssistente = cdAssistente;
    }

    public String getNmNome() {
        return nmNome;
    }

    public void setNmNome(String nmNome) {
        this.nmNome = nmNome;
    }

    public String getDsDescricao() {
        return dsDescricao;
    }

    public void setDsDescricao(String dsDescricao) {
        this.dsDescricao = dsDescricao;
    }

    public String getDsSystemPrompt() {
        return dsSystemPrompt;
    }

    public void setDsSystemPrompt(String dsSystemPrompt) {
        this.dsSystemPrompt = dsSystemPrompt;
    }

    public String getNmModelo() {
        return nmModelo;
    }

    public void setNmModelo(String nmModelo) {
        this.nmModelo = nmModelo;
    }

    public boolean isFlAtivo() {
        return flAtivo;
    }

    public void setFlAtivo(boolean flAtivo) {
        this.flAtivo = flAtivo;
    }

    public boolean isFlRagInject() {
        return flRagInject;
    }

    public void setFlRagInject(boolean flRagInject) {
        this.flRagInject = flRagInject;
    }

    public int getNuRagTopK() {
        return nuRagTopK;
    }

    public void setNuRagTopK(int nuRagTopK) {
        this.nuRagTopK = nuRagTopK;
    }
}
