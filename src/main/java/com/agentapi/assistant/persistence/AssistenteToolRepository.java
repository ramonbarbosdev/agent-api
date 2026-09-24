package com.agentapi.assistant.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssistenteToolRepository extends JpaRepository<AssistenteToolEntity, UUID> {

    List<AssistenteToolEntity> findByIdAssistenteOrderByNmToolAsc(UUID idAssistente);

    @Modifying
    @Query("DELETE FROM AssistenteToolEntity t WHERE t.idAssistente = :idAssistente")
    void deleteByIdAssistente(@Param("idAssistente") UUID idAssistente);
}
