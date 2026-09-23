package com.agentapi.agent.context;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;

@Component
public class NoOpConversationMemoryProvider implements ConversationMemoryProvider {

    @Override
    public Optional<String> longTermMemory(AgentContext context) {
        return Optional.empty();
    }
}
