package com.agentapi.auth.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario", schema = "agent")
public class UsuarioEntity {

    @Id
    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "ds_email", nullable = false, unique = true)
    private String email;

    @Column(name = "ds_password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
