package com.agentapi.agent.context;

import java.util.Optional;

import com.agentapi.agent.AgentContext;

/**
 * Recuperação de documentos (fase 6). Stub até RAG existir.
 */
public interface RagContextProvider {

    Optional<String> retrievalContext(AgentContext context, String userMessage);
}
