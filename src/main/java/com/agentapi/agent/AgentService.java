package com.agentapi.agent;

import org.springframework.stereotype.Service;

import com.agentapi.assistant.AssistantType;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;

@Service
public class AgentService {

    private final AgentEngine agentEngine;

    public AgentService(AgentEngine agentEngine) {
        this.agentEngine = agentEngine;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        AssistantType assistantType = AssistantType.fromString(request.getAssistant())
                .orElseThrow(() -> new AssistantNotFoundException(request.getAssistant()));

        AgentContext context = AgentContext.builder()
                .assistant(assistantType)
                .build();

        String reply = agentEngine.run(context, request.getMessage());
        return new AgentChatResponse(reply);
    }
}
