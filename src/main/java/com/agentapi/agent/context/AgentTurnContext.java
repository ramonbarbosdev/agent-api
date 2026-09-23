package com.agentapi.agent.context;

import java.util.List;

import com.agentapi.llm.LlmMessage;
import com.agentapi.llm.LlmToolDefinition;

/**
 * Snapshot do que o LLM recebe no início de um turno (antes do loop de tools).
 */
public record AgentTurnContext(
        String model,
        List<LlmMessage> messages,
        List<LlmToolDefinition> tools,
        int historyMessagesIncluded,
        int historyMessagesDropped) {

    public List<LlmMessage> mutableMessages() {
        return new java.util.ArrayList<>(messages);
    }
}
