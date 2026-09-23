package com.agentapi.tool;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.AssistantType;
import com.fasterxml.jackson.databind.JsonNode;

public interface AgentTool {

    String name();

    String description();

    ToolKind kind();

    /**
     * Assistentes que podem usar esta tool. Vazio = disponivel para todos.
     */
    default Set<AssistantType> assistants() {
        return Collections.emptySet();
    }

    /**
     * JSON Schema dos parametros (objeto), para documentacao e futuro tool calling do LLM.
     */
    Map<String, Object> parametersSchema();

    ToolResult execute(AgentContext context, JsonNode arguments);

    default boolean supportsAssistant(AssistantType assistant) {
        Set<AssistantType> allowed = assistants();
        return allowed == null || allowed.isEmpty() || allowed.contains(assistant);
    }
}
