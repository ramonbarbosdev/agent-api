package com.agentapi.rag.persistence;

import java.util.UUID;

import com.agentapi.model.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento", schema = "agent")
public class DocumentoEntity extends AuditableEntity {

    @Id
    @Column(name = "id_documento", nullable = false)
    private UUID idDocumento;

    @Column(name = "nm_titulo", nullable = false)
    private String nmTitulo;

    @Column(name = "nm_fonte", nullable = false)
    private String nmFonte;

    protected DocumentoEntity() {
    }

    public DocumentoEntity(UUID idDocumento, String nmTitulo, String nmFonte) {
        this.idDocumento = idDocumento;
        this.nmTitulo = nmTitulo;
        this.nmFonte = nmFonte;
    }

    public UUID getIdDocumento() {
        return idDocumento;
    }

    public String getNmTitulo() {
        return nmTitulo;
    }

    public String getNmFonte() {
        return nmFonte;
    }
}
