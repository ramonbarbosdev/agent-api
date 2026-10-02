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

    Optional<AssistenteEntity> findByCdAssistenteIgnoreCaseAndIdUsuario(String cdAssistente, UUID idUsuario);

    List<AssistenteEntity> findByIdUsuarioAndFlAtivoTrueOrderByNmNomeAsc(UUID idUsuario);

    List<AssistenteEntity> findByIdUsuarioOrderByNmNomeAsc(UUID idUsuario);

    boolean existsByCdAssistenteIgnoreCaseAndIdUsuario(String cdAssistente, UUID idUsuario);

    long countByIdUsuario(UUID idUsuario);
}
