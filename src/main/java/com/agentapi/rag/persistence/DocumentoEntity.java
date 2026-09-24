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

    @Column(name = "ds_conteudo", nullable = false, columnDefinition = "TEXT")
    private String dsConteudo;

    protected DocumentoEntity() {
    }

    public DocumentoEntity(UUID idDocumento, String nmTitulo, String nmFonte, String dsConteudo) {
        this.idDocumento = idDocumento;
        this.nmTitulo = nmTitulo;
        this.nmFonte = nmFonte;
        this.dsConteudo = dsConteudo;
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

    public String getDsConteudo() {
        return dsConteudo;
    }

    public void setNmTitulo(String nmTitulo) {
        this.nmTitulo = nmTitulo;
    }

    public void setNmFonte(String nmFonte) {
        this.nmFonte = nmFonte;
    }

    public void setDsConteudo(String dsConteudo) {
        this.dsConteudo = dsConteudo;
    }
}
