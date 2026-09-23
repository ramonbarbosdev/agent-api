package com.agentapi.agent;

import org.springframework.stereotype.Service;

import java.util.UUID;

import com.agentapi.assistant.AssistantType;
import com.agentapi.conversation.ConversationService;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;

@Service
public class AgentService {

    private final AgentEngine agentEngine;
    private final ConversationService conversationService;

    public AgentService(AgentEngine agentEngine, ConversationService conversationService) {
        this.agentEngine = agentEngine;
        this.conversationService = conversationService;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        AssistantType assistantType = AssistantType.fromString(request.getAssistant())
                .orElseThrow(() -> new AssistantNotFoundException(request.getAssistant()));

        UUID conversationId = conversationService.resolveConversationId(request.getConversationId());

        AgentContext context = AgentContext.builder()
                .assistant(assistantType)
                .conversationId(conversationId)
                .build();

        conversationService.ensureConversation(conversationId, assistantType, context.getUserId());

        String reply = agentEngine.run(context, request.getMessage(), request.getHistory());

        conversationService.appendTurn(conversationId, request.getMessage(), reply);

        return new AgentChatResponse(reply, conversationId.toString());
    }
}
