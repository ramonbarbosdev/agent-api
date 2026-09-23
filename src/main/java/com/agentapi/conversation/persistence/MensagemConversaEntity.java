package com.agentapi.conversation.persistence;

import java.util.UUID;

import com.agentapi.model.AuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mensagem_conversa", schema = "agent")
public class MensagemConversaEntity extends AuditableEntity {

    @Id
    @Column(name = "id_mensagemconversa", nullable = false)
    private UUID idMensagemConversa;

    @Column(name = "id_conversa", nullable = false)
    private UUID idConversa;

    @Column(name = "tp_papel", nullable = false, length = 16)
    private String tpPapel;

    @Column(name = "ds_conteudo", nullable = false, columnDefinition = "TEXT")
    private String dsConteudo;

    protected MensagemConversaEntity() {
    }

    public MensagemConversaEntity(UUID idMensagemConversa, UUID idConversa, String tpPapel, String dsConteudo) {
        this.idMensagemConversa = idMensagemConversa;
        this.idConversa = idConversa;
        this.tpPapel = tpPapel;
        this.dsConteudo = dsConteudo;
    }

    public UUID getIdMensagemConversa() {
        return idMensagemConversa;
    }

    public UUID getIdConversa() {
        return idConversa;
    }

    public String getTpPapel() {
        return tpPapel;
    }

    public String getDsConteudo() {
        return dsConteudo;
    }
}
