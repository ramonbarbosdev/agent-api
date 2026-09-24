package com.agentapi.tool;

import org.springframework.stereotype.Component;

@Component
public class ToolCatalogFormatter {

    private final ToolRegistry toolRegistry;

    public ToolCatalogFormatter(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public String formatForAssistant(String assistantCode) {
        StringBuilder sb = new StringBuilder();
        for (AgentTool tool : toolRegistry.listForAssistant(assistantCode)) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append("- ").append(tool.name()).append(" (").append(tool.kind()).append("): ");
            sb.append(tool.description());
        }
        return sb.toString();
    }
}
