package com.agentapi.agent.context;

import java.util.Optional;

import com.agentapi.agent.AgentContext;

/**
 * Memória de longo prazo / resumo da thread (fase 5+). Implementações futuras podem usar DB ou LLM.
 */
public interface ConversationMemoryProvider {

    Optional<String> longTermMemory(AgentContext context);
}
