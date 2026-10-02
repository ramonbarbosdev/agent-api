package com.agentapi.agent;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.AssistantService;
import com.agentapi.auth.CurrentUser;
import com.agentapi.conversation.ConversationService;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;
import com.agentapi.web.AgentChatStreamRequest;

@Service
public class AgentService {

    private final CursorAgentRuntime cursorAgentRuntime;
    private final ConversationService conversationService;
    private final AssistantService assistantService;
    private final CurrentUser currentUser;

    public AgentService(
            CursorAgentRuntime cursorAgentRuntime,
            ConversationService conversationService,
            AssistantService assistantService,
            CurrentUser currentUser) {
        this.cursorAgentRuntime = cursorAgentRuntime;
        this.conversationService = conversationService;
        this.assistantService = assistantService;
        this.currentUser = currentUser;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        String assistantCode = AssistantCodes.normalize(request.getAssistant());
        if (assistantCode.isBlank()) {
            throw new AssistantNotFoundException(request.getAssistant());
        }
        UUID userId = currentUser.requireUserId();
        assistantService.requireActiveByCodeForUser(assistantCode, userId);

        UUID conversationId = conversationService.resolveConversationId(request.getConversationId());
        AgentContext context = AgentContext.builder()
                .userId(userId)
                .assistantCode(assistantCode)
                .conversationId(conversationId)
                .build();

        conversationService.ensureConversation(conversationId, assistantCode, userId);

        String reply = cursorAgentRuntime.run(context, request.getMessage(), request.getHistory());
        conversationService.appendTurn(conversationId, request.getMessage(), reply);
        return new AgentChatResponse(reply, conversationId.toString());
    }

    public AgentChatResponse chatStream(AgentChatStreamRequest request, AgentStreamEmitter emitter) {
        String assistantCode = AssistantCodes.normalize(request.getAssistant());
        if (assistantCode.isBlank()) {
            throw new AssistantNotFoundException(request.getAssistant());
        }
        UUID userId = currentUser.requireUserId();
        assistantService.requireActiveByCodeForUser(assistantCode, userId);

        UUID conversationId = conversationService.resolveConversationId(request.getConversationId());
        AgentContext context = AgentContext.builder()
                .userId(userId)
                .assistantCode(assistantCode)
                .conversationId(conversationId)
                .build();

        conversationService.ensureConversation(conversationId, assistantCode, userId);

        String reply = cursorAgentRuntime.runStream(context, request.getMessage(), List.of(), emitter);
        conversationService.appendTurn(conversationId, request.getMessage(), reply);
        return new AgentChatResponse(reply, conversationId.toString());
    }
}
