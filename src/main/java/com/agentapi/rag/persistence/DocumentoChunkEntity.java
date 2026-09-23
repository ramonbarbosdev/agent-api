package com.agentapi.rag.persistence;

import java.util.UUID;

import com.agentapi.model.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento_chunk", schema = "agent")
public class DocumentoChunkEntity extends AuditableEntity {

    @Id
    @Column(name = "id_documentochunk", nullable = false)
    private UUID idDocumentoChunk;

    @Column(name = "id_documento", nullable = false)
    private UUID idDocumento;

    @Column(name = "nu_ordem", nullable = false)
    private int nuOrdem;

    @Column(name = "ds_conteudo", nullable = false, columnDefinition = "TEXT")
    private String dsConteudo;

    protected DocumentoChunkEntity() {
    }

    public DocumentoChunkEntity(UUID idDocumentoChunk, UUID idDocumento, int nuOrdem, String dsConteudo) {
        this.idDocumentoChunk = idDocumentoChunk;
        this.idDocumento = idDocumento;
        this.nuOrdem = nuOrdem;
        this.dsConteudo = dsConteudo;
    }

    public UUID getIdDocumentoChunk() {
        return idDocumentoChunk;
    }

    public UUID getIdDocumento() {
        return idDocumento;
    }

    public int getNuOrdem() {
        return nuOrdem;
    }

    public String getDsConteudo() {
        return dsConteudo;
    }
}
