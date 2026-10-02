package com.agentapi.gateway.session;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewaySessionRepository extends JpaRepository<GatewaySessionEntity, UUID> {

    Optional<GatewaySessionEntity> findByIdConversa(UUID idConversa);

    long countByIdUsuarioAndStatusIn(UUID idUsuario, Iterable<String> statuses);
}
