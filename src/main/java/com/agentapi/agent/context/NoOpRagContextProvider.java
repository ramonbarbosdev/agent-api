package com.agentapi.agent.context;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;

@Component
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "false")
public class NoOpRagContextProvider implements RagContextProvider {

    @Override
    public Optional<String> retrievalContext(AgentContext context, String userMessage) {
        return Optional.empty();
    }
}
