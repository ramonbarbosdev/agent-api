package com.agentapi.agent.context;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.config.RagProperties;
import com.agentapi.rag.RagContextFormatter;
import com.agentapi.rag.RagHit;
import com.agentapi.rag.RagSearchService;

@Component
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseRagContextProvider implements RagContextProvider {

    private final RagProperties ragProperties;
    private final RagSearchService ragSearchService;

    public DatabaseRagContextProvider(RagProperties ragProperties, RagSearchService ragSearchService) {
        this.ragProperties = ragProperties;
        this.ragSearchService = ragSearchService;
    }

    @Override
    public Optional<String> retrievalContext(AgentContext context, String userMessage) {
        if (!ragProperties.isInjectIntoSystemPrompt()) {
            return Optional.empty();
        }
        List<RagHit> hits = ragSearchService.search(userMessage);
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(RagContextFormatter.formatForPrompt(hits));
    }
}
