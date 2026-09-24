package com.agentapi.tool;

import java.util.List;

import org.springframework.stereotype.Service;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.AssistantService;
import com.agentapi.web.ToolDescriptorDto;
import com.agentapi.web.ToolInvokeResponse;

@Service
public class ToolService {

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final AssistantService assistantService;

    public ToolService(
            ToolRegistry toolRegistry,
            ToolExecutor toolExecutor,
            AssistantService assistantService) {
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.assistantService = assistantService;
    }

    public List<ToolDescriptorDto> listForAssistant(String assistantParam) {
        String code = resolveAssistantCode(assistantParam);
        return toolRegistry.listForAssistant(code).stream()
                .map(tool -> new ToolDescriptorDto(
                        tool.name(),
                        tool.description(),
                        tool.kind(),
                        tool.parametersSchema()))
                .toList();
    }

    public ToolInvokeResponse invoke(String assistantParam, String toolName, String argumentsJson) {
        String code = resolveAssistantCode(assistantParam);
        AgentContext context = AgentContext.builder()
                .assistantCode(code)
                .build();
        ToolResult result = toolExecutor.execute(context, toolName, argumentsJson);
        return new ToolInvokeResponse(result.success(), result.content());
    }

    private String resolveAssistantCode(String assistantParam) {
        if (assistantParam == null || assistantParam.isBlank()) {
            return assistantService.listActive().stream()
                    .findFirst()
                    .map(a -> a.code())
                    .orElseThrow(() -> new com.agentapi.exception.AssistantNotFoundException(assistantParam));
        }
        assistantService.requireActiveByCode(assistantParam);
        return com.agentapi.assistant.AssistantCodes.normalize(assistantParam);
    }
}
