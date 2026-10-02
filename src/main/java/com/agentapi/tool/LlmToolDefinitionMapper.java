package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LlmToolDefinitionMapper {

    private final ToolRegistry toolRegistry;

    public LlmToolDefinitionMapper(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public List<ToolDefinitionDto> definitionsFor(String assistantCode) {
        return toolRegistry.listForAssistant(assistantCode).stream()
                .map(tool -> new ToolDefinitionDto(
                        tool.name(),
                        tool.description(),
                        tool.parametersSchema()))
                .toList();
    }
}
