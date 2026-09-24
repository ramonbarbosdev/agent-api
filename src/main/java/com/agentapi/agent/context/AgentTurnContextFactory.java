package com.agentapi.agent.context;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.Assistant;
import com.agentapi.config.AgentEngineProperties;
import com.agentapi.config.OllamaProperties;
import com.agentapi.conversation.ConversationService;
import com.agentapi.llm.LlmMessage;
import com.agentapi.llm.LlmToolDefinition;
import com.agentapi.tool.LlmToolDefinitionMapper;
import com.agentapi.web.AgentChatHistoryMessage;

@Component
public class AgentTurnContextFactory {

    private static final Logger log = LoggerFactory.getLogger(AgentTurnContextFactory.class);

    private final ConversationService conversationService;
    private final AgentSystemPromptComposer systemPromptComposer;
    private final LlmToolDefinitionMapper toolDefinitionMapper;
    private final ContextWindowTrimmer contextWindowTrimmer;
    private final OllamaProperties ollamaProperties;
    private final AgentEngineProperties engineProperties;

    public AgentTurnContextFactory(
            ConversationService conversationService,
            AgentSystemPromptComposer systemPromptComposer,
            LlmToolDefinitionMapper toolDefinitionMapper,
            ContextWindowTrimmer contextWindowTrimmer,
            OllamaProperties ollamaProperties,
            AgentEngineProperties engineProperties) {
        this.conversationService = conversationService;
        this.systemPromptComposer = systemPromptComposer;
        this.toolDefinitionMapper = toolDefinitionMapper;
        this.contextWindowTrimmer = contextWindowTrimmer;
        this.ollamaProperties = ollamaProperties;
        this.engineProperties = engineProperties;
    }

    public AgentTurnContext build(
            AgentContext context,
            Assistant assistant,
            String userMessage,
            List<AgentChatHistoryMessage> clientHistoryFallback) {

        List<AgentChatHistoryMessage> resolvedHistory = conversationService.resolveHistoryForLlm(
                context.getConversationId(),
                clientHistoryFallback);

        String systemPrompt = systemPromptComposer.compose(assistant, context, userMessage);

        ContextWindowTrimmer.TrimResult trim = contextWindowTrimmer.trimHistory(
                resolvedHistory,
                engineProperties.getMaxContextChars(),
                systemPrompt,
                userMessage);

        if (trim.droppedCount() > 0) {
            log.info(
                    "Context window trimmed (conversationId={}, droppedMessages={}, kept={})",
                    context.getConversationId(),
                    trim.droppedCount(),
                    trim.history().size());
        }

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(systemPrompt));
        for (AgentChatHistoryMessage item : trim.history()) {
            if ("user".equals(item.getRole())) {
                messages.add(LlmMessage.user(item.getContent()));
            } else if ("assistant".equals(item.getRole())) {
                messages.add(LlmMessage.assistant(item.getContent()));
            }
        }
        messages.add(LlmMessage.user(userMessage));

        String model = assistant.resolveModel(ollamaProperties.getModel());
        List<LlmToolDefinition> tools = toolDefinitionMapper.definitionsFor(assistant.code());

        return new AgentTurnContext(
                model,
                messages,
                tools,
                trim.history().size(),
                trim.droppedCount());
    }
}
