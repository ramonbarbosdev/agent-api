package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Service;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.AssistantType;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.web.ToolDescriptorDto;
import com.agentapi.web.ToolInvokeResponse;

@Service
public class ToolService {

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;

    public ToolService(ToolRegistry toolRegistry, ToolExecutor toolExecutor) {
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
    }

    public List<ToolDescriptorDto> listForAssistant(String assistantParam) {
        AssistantType assistant = resolveAssistant(assistantParam);
        return toolRegistry.listForAssistant(assistant).stream()
                .map(tool -> new ToolDescriptorDto(
                        tool.name(),
                        tool.description(),
                        tool.kind(),
                        tool.parametersSchema()))
                .toList();
    }

    public ToolInvokeResponse invoke(AssistantType assistantType, String toolName, String argumentsJson) {
        AgentContext context = AgentContext.builder()
                .assistant(assistantType)
                .build();
        ToolResult result = toolExecutor.execute(context, toolName, argumentsJson);
        return new ToolInvokeResponse(result.success(), result.content());
    }

    private static AssistantType resolveAssistant(String assistantParam) {
        if (assistantParam == null || assistantParam.isBlank()) {
            return AssistantType.HORAS_EXTRAS;
        }
        return AssistantType.fromString(assistantParam)
                .orElseThrow(() -> new AssistantNotFoundException(assistantParam));
    }
}
