package com.agentapi.agent;

import org.springframework.stereotype.Service;

import java.util.UUID;

import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.AssistantService;
import com.agentapi.conversation.ConversationService;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;
import com.agentapi.web.AgentChatStreamRequest;

import java.util.List;

@Service
public class AgentService {

    private final AgentEngine agentEngine;
    private final ConversationService conversationService;
    private final AssistantService assistantService;

    public AgentService(
            AgentEngine agentEngine,
            ConversationService conversationService,
            AssistantService assistantService) {
        this.agentEngine = agentEngine;
        this.conversationService = conversationService;
        this.assistantService = assistantService;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        String assistantCode = AssistantCodes.normalize(request.getAssistant());
        if (assistantCode.isBlank()) {
            throw new AssistantNotFoundException(request.getAssistant());
        }
        assistantService.requireActiveByCode(assistantCode);

        UUID conversationId = conversationService.resolveConversationId(request.getConversationId());

        AgentContext context = AgentContext.builder()
                .assistantCode(assistantCode)
                .conversationId(conversationId)
                .build();

        conversationService.ensureConversation(conversationId, assistantCode, context.getUserId());

        String reply = agentEngine.run(context, request.getMessage(), request.getHistory());

        conversationService.appendTurn(conversationId, request.getMessage(), reply);

        return new AgentChatResponse(reply, conversationId.toString());
    }

    public AgentChatResponse chatStream(AgentChatStreamRequest request, AgentStreamEmitter emitter) {
        String assistantCode = AssistantCodes.normalize(request.getAssistant());
        if (assistantCode.isBlank()) {
            throw new AssistantNotFoundException(request.getAssistant());
        }
        assistantService.requireActiveByCode(assistantCode);

        UUID conversationId = conversationService.resolveConversationId(request.getConversationId());

        AgentContext context = AgentContext.builder()
                .assistantCode(assistantCode)
                .conversationId(conversationId)
                .build();

        conversationService.ensureConversation(conversationId, assistantCode, context.getUserId());

        String reply = agentEngine.runStream(context, request.getMessage(), List.of(), emitter);

        conversationService.appendTurn(conversationId, request.getMessage(), reply);

        return new AgentChatResponse(reply, conversationId.toString());
    }
}
