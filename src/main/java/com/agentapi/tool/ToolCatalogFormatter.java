package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Component;

import com.agentapi.assistant.AssistantType;

@Component
public class ToolCatalogFormatter {

    private final ToolRegistry toolRegistry;

    public ToolCatalogFormatter(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    public String formatForAssistant(AssistantType assistant) {
        List<AgentTool> tools = toolRegistry.listForAssistant(assistant);
        if (tools.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Ferramentas registradas na API (invocadas via tool calling do modelo; ");
        sb.append("nunca invente resultado de ferramenta):\n");
        for (AgentTool tool : tools) {
            sb.append("- ").append(tool.name())
                    .append(" [").append(tool.kind().name()).append("]: ")
                    .append(tool.description())
                    .append("\n");
        }
        return sb.toString().trim();
    }
}
