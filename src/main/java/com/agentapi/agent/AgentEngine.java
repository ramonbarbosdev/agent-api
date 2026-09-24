package com.agentapi.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.agentapi.agent.context.AgentTurnContext;
import com.agentapi.agent.context.AgentTurnContextFactory;
import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantService;
import com.agentapi.config.AgentEngineProperties;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.exception.ToolException;
import com.agentapi.llm.LlmClient;
import com.agentapi.llm.LlmMessage;
import com.agentapi.llm.LlmRequest;
import com.agentapi.llm.LlmResponse;
import com.agentapi.llm.LlmToolCall;
import com.agentapi.tool.ToolExecutor;
import com.agentapi.tool.ToolResult;
import com.agentapi.web.AgentChatHistoryMessage;

@Component
public class AgentEngine {

    private static final Logger log = LoggerFactory.getLogger(AgentEngine.class);

    private final AssistantService assistantService;
    private final LlmClient llmClient;
    private final ToolExecutor toolExecutor;
    private final AgentTurnContextFactory turnContextFactory;
    private final AgentEngineProperties engineProperties;

    public AgentEngine(
            AssistantService assistantService,
            LlmClient llmClient,
            ToolExecutor toolExecutor,
            AgentTurnContextFactory turnContextFactory,
            AgentEngineProperties engineProperties) {
        this.assistantService = assistantService;
        this.llmClient = llmClient;
        this.toolExecutor = toolExecutor;
        this.turnContextFactory = turnContextFactory;
        this.engineProperties = engineProperties;
    }

    public String run(AgentContext context, String userMessage, List<AgentChatHistoryMessage> clientHistoryFallback) {
        log.info("Agent request received (assistant={}, conversationId={})",
                context.getAssistantCode(), context.getConversationId());

        Assistant assistant = assistantService.findActiveByCode(context.getAssistantCode())
                .orElseThrow(() -> new AssistantNotFoundException(context.getAssistantCode()));

        AgentTurnContext turn = turnContextFactory.build(context, assistant, userMessage, clientHistoryFallback);

        log.info(
                "Turn context assembled (assistant={}, historyMessages={}, dropped={}, tools={})",
                assistant.code(),
                turn.historyMessagesIncluded(),
                turn.historyMessagesDropped(),
                turn.tools().size());

        List<LlmMessage> messages = turn.mutableMessages();
        String model = turn.model();
        var tools = turn.tools();

        int maxSteps = Math.max(1, engineProperties.getMaxToolSteps());
        LlmResponse lastResponse = null;

        for (int step = 0; step < maxSteps; step++) {
            LlmRequest request = tools.isEmpty()
                    ? new LlmRequest(model, messages)
                    : new LlmRequest(model, messages, tools);

            log.info("LLM request started (assistant={}, model={}, step={}, tools={})",
                    assistant.code(), model, step + 1, tools.size());

            long started = System.currentTimeMillis();
            lastResponse = llmClient.chat(request);
            log.info("LLM response received (assistant={}, step={}, toolCalls={}, latencyMs={})",
                    assistant.code(),
                    step + 1,
                    lastResponse.toolCalls().size(),
                    System.currentTimeMillis() - started);

            if (!lastResponse.hasToolCalls()) {
                break;
            }

            messages.add(LlmMessage.assistantToolCalls(lastResponse.content(), lastResponse.toolCalls()));
            appendToolResults(context, messages, lastResponse.toolCalls());
        }

        if (lastResponse == null) {
            return "Não foi possível obter resposta do assistente.";
        }

        if (lastResponse.hasToolCalls()) {
            log.warn("Max tool steps ({}) reached; returning partial assistant text", maxSteps);
            if (!lastResponse.content().isBlank()) {
                return lastResponse.content();
            }
            return "Não consegui concluir a operação dentro do limite de passos de ferramentas. Tente reformular o pedido.";
        }

        log.info("Agent request completed (assistant={})", assistant.code());
        return lastResponse.content();
    }

    public String runStream(
            AgentContext context,
            String userMessage,
            List<AgentChatHistoryMessage> clientHistoryFallback,
            AgentStreamEmitter emitter) {
        log.info("Agent stream request received (assistant={}, conversationId={})",
                context.getAssistantCode(), context.getConversationId());

        Assistant assistant = assistantService.findActiveByCode(context.getAssistantCode())
                .orElseThrow(() -> new AssistantNotFoundException(context.getAssistantCode()));

        AgentTurnContext turn = turnContextFactory.build(context, assistant, userMessage, clientHistoryFallback);

        List<LlmMessage> messages = turn.mutableMessages();
        String model = turn.model();
        var tools = turn.tools();

        int maxSteps = Math.max(1, engineProperties.getMaxToolSteps());
        LlmResponse lastResponse = null;

        for (int step = 0; step < maxSteps; step++) {
            LlmRequest request = tools.isEmpty()
                    ? new LlmRequest(model, messages)
                    : new LlmRequest(model, messages, tools);

            log.info("LLM stream step started (assistant={}, model={}, step={}, tools={})",
                    assistant.code(), model, step + 1, tools.size());

            long started = System.currentTimeMillis();
            if (tools.isEmpty()) {
                lastResponse = llmClient.chatStream(request, emitter::onToken);
            } else {
                if (step > 0) {
                    emitter.onPhase("Processando resultado das ferramentas…");
                } else {
                    emitter.onPhase("Consultando ferramentas…");
                }
                lastResponse = llmClient.chat(request);
                String content = lastResponse.content();
                if (content != null && !content.isEmpty()) {
                    emitter.onToken(content);
                }
            }

            log.info("LLM stream step finished (assistant={}, step={}, toolCalls={}, latencyMs={})",
                    assistant.code(),
                    step + 1,
                    lastResponse.toolCalls().size(),
                    System.currentTimeMillis() - started);

            if (!lastResponse.hasToolCalls()) {
                break;
            }

            messages.add(LlmMessage.assistantToolCalls(lastResponse.content(), lastResponse.toolCalls()));
            appendToolResults(context, messages, lastResponse.toolCalls());
        }

        if (lastResponse == null) {
            return "Não foi possível obter resposta do assistente.";
        }

        if (lastResponse.hasToolCalls()) {
            log.warn("Max tool steps ({}) reached during stream", maxSteps);
            if (!lastResponse.content().isBlank()) {
                return lastResponse.content();
            }
            return "Não consegui concluir a operação dentro do limite de passos de ferramentas. Tente reformular o pedido.";
        }

        log.info("Agent stream completed (assistant={})", assistant.code());
        return lastResponse.content();
    }

    private void appendToolResults(AgentContext context, List<LlmMessage> messages, List<LlmToolCall> toolCalls) {
        for (LlmToolCall call : toolCalls) {
            String payload = executeToolCall(context, call);
            messages.add(LlmMessage.tool(payload));
        }
    }

    private String executeToolCall(AgentContext context, LlmToolCall call) {
        try {
            ToolResult result = toolExecutor.execute(context, call.name(), call.argumentsJson());
            if (result.success()) {
                return result.content();
            }
            return "{\"error\":true,\"message\":\"" + escapeJson(result.content()) + "\"}";
        } catch (ToolException ex) {
            log.warn("Tool call failed (tool={}): {}", call.name(), ex.getMessage());
            return "{\"error\":true,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}";
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
