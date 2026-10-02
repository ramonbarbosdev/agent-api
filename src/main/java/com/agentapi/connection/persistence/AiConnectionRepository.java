package com.agentapi.connection.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConnectionRepository extends JpaRepository<AiConnectionEntity, UUID> {

    List<AiConnectionEntity> findByIdUsuarioOrderByCriadoEmDesc(UUID idUsuario);

    Optional<AiConnectionEntity> findByIdConnectionAndIdUsuario(UUID idConnection, UUID idUsuario);

    Optional<AiConnectionEntity> findFirstByIdUsuarioAndActiveTrueOrderByAtualizadoEmDesc(UUID idUsuario);
}
