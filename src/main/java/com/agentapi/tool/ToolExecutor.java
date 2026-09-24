package com.agentapi.tool;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.agent.AgentPolicy;
import com.agentapi.exception.ErrorCode;
import com.agentapi.exception.ToolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutor.class);

    private final ToolRegistry toolRegistry;
    private final AgentPolicy agentPolicy;
    private final ObjectMapper objectMapper;

    public ToolExecutor(ToolRegistry toolRegistry, AgentPolicy agentPolicy, ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.agentPolicy = agentPolicy;
        this.objectMapper = objectMapper;
    }

    public ToolResult execute(AgentContext context, String toolName, String argumentsJson) {
        AgentTool tool = toolRegistry.getByName(toolName)
                .orElseThrow(() -> new ToolException(
                        ErrorCode.TOOL_NOT_FOUND,
                        "Ferramenta não encontrada: " + toolName));

        Set<String> allowed = toolRegistry.listForAssistant(context.getAssistantCode()).stream()
                .map(AgentTool::name)
                .collect(Collectors.toSet());
        if (!allowed.contains(toolName)) {
            throw new ToolException(
                    ErrorCode.TOOL_NOT_ALLOWED,
                    "A ferramenta " + toolName + " não está disponível para o assistente "
                            + context.getAssistantCode());
        }

        if (!agentPolicy.canExecuteTool(context, tool)) {
            throw new ToolException(
                    ErrorCode.TOOL_NOT_ALLOWED,
                    "Execução não autorizada para a ferramenta " + toolName
                            + " (operação de escrita requer confirmação futura).");
        }

        JsonNode arguments = parseArguments(argumentsJson);

        log.info("Tool execution started (tool={}, assistant={}, conversationId={})",
                toolName, context.getAssistantCode(), context.getConversationId());

        long started = System.currentTimeMillis();
        try {
            ToolResult result = tool.execute(context, arguments);
            log.info("Tool execution finished (tool={}, success={}, latencyMs={})",
                    toolName, result.success(), System.currentTimeMillis() - started);
            return result;
        } catch (ToolException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Tool execution failed (tool={})", toolName, ex);
            throw new ToolException(
                    ErrorCode.TOOL_EXECUTION_ERROR,
                    "Falha ao executar " + toolName + ": " + ex.getMessage());
        }
    }

    private JsonNode parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(argumentsJson);
        } catch (JsonProcessingException ex) {
            throw new ToolException(ErrorCode.INVALID_REQUEST, "Argumentos da ferramenta não são JSON válido.");
        }
    }
}
