package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Component;

import com.agentapi.llm.LlmToolDefinition;

@Component
public class LlmToolDefinitionMapper {

    private final ToolRegistry toolRegistry;

    public LlmToolDefinitionMapper(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public List<LlmToolDefinition> definitionsFor(String assistantCode) {
        return toolRegistry.listForAssistant(assistantCode).stream()
                .map(tool -> new LlmToolDefinition(
                        tool.name(),
                        tool.description(),
                        tool.parametersSchema()))
                .toList();
    }
}
