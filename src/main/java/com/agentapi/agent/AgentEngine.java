package com.agentapi.agent;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantService;
import com.agentapi.config.OllamaProperties;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.llm.LlmClient;
import com.agentapi.llm.LlmMessage;
import com.agentapi.llm.LlmRequest;
import com.agentapi.llm.LlmResponse;
import com.agentapi.tool.ToolExecutor;
import com.agentapi.tool.ToolRegistry;
import com.agentapi.web.AgentChatHistoryMessage;

@Component
public class AgentEngine {

    private static final Logger log = LoggerFactory.getLogger(AgentEngine.class);

    private final AssistantService assistantService;
    private final LlmClient llmClient;
    private final OllamaProperties ollamaProperties;
    @SuppressWarnings("unused")
    private final ToolRegistry toolRegistry;
    @SuppressWarnings("unused")
    private final ToolExecutor toolExecutor;

    public AgentEngine(
            AssistantService assistantService,
            LlmClient llmClient,
            OllamaProperties ollamaProperties,
            ToolRegistry toolRegistry,
            ToolExecutor toolExecutor) {
        this.assistantService = assistantService;
        this.llmClient = llmClient;
        this.ollamaProperties = ollamaProperties;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
    }

    public String run(AgentContext context, String userMessage, List<AgentChatHistoryMessage> history) {
        log.info("Agent request received (assistant={})", context.getAssistant());

        Assistant assistant = assistantService.find(context.getAssistant())
                .orElseThrow(() -> new AssistantNotFoundException(context.getAssistant().name()));

        log.info("Assistant selected (assistant={})", assistant.type().name());

        String model = assistant.resolveModel(ollamaProperties.getModel());
        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(assistant.systemPrompt()));
        if (history != null) {
            for (AgentChatHistoryMessage item : history) {
                if ("user".equals(item.getRole())) {
                    messages.add(LlmMessage.user(item.getContent()));
                } else if ("assistant".equals(item.getRole())) {
                    messages.add(LlmMessage.assistant(item.getContent()));
                }
            }
        }
        messages.add(LlmMessage.user(userMessage));

        LlmRequest request = new LlmRequest(model, messages);

        log.info("LLM request started (assistant={}, model={}, messageLength={})",
                assistant.type().name(), model, userMessage.length());

        long started = System.currentTimeMillis();
        LlmResponse response = llmClient.chat(request);
        long elapsed = System.currentTimeMillis() - started;

        log.info("LLM response received (assistant={}, latencyMs={})", assistant.type().name(), elapsed);

        handleToolCalls(context, response.content());

        log.info("Agent request completed (assistant={})", assistant.type().name());
        return response.content();
    }

    private void handleToolCalls(AgentContext context, String llmContent) {
        // Future: parse tool calls from llmContent, authorize via AgentPolicy,
        // then run toolRegistry + toolExecutor and send results back to the LLM.
    }
}
