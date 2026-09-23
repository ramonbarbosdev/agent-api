package com.agentapi.conversation.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversaRepository extends JpaRepository<ConversaEntity, UUID> {
}
