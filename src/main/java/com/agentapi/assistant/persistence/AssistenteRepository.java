package com.agentapi.assistant.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistenteRepository extends JpaRepository<AssistenteEntity, UUID> {

    Optional<AssistenteEntity> findByCdAssistenteIgnoreCase(String cdAssistente);

    List<AssistenteEntity> findByFlAtivoTrueOrderByNmNomeAsc();

    List<AssistenteEntity> findAllByOrderByNmNomeAsc();

    boolean existsByCdAssistenteIgnoreCase(String cdAssistente);
}
