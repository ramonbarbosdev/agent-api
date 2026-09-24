package com.agentapi.agent;

/**
 * Recebe eventos de um turno do agente (tokens do LLM, fases de tool, conclusão).
 */
public interface AgentStreamEmitter {

    void onPhase(String message);

    void onToken(String token);

    void onDone(String fullMessage);
}
