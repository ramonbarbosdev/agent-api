package com.agentapi.rag.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoRepository extends JpaRepository<DocumentoEntity, UUID> {
}
