package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Component;

import com.agentapi.assistant.AssistantType;
import com.agentapi.llm.LlmToolDefinition;

@Component
public class LlmToolDefinitionMapper {

    private final ToolRegistry toolRegistry;

    public LlmToolDefinitionMapper(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public List<LlmToolDefinition> definitionsFor(AssistantType assistant) {
        return toolRegistry.listForAssistant(assistant).stream()
                .map(tool -> new LlmToolDefinition(tool.name(), tool.description(), tool.parametersSchema()))
                .toList();
    }
}
