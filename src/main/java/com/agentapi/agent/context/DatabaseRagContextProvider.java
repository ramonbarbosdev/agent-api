package com.agentapi.agent.context;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantService;
import com.agentapi.config.RagProperties;
import com.agentapi.rag.RagContextFormatter;
import com.agentapi.rag.RagHit;
import com.agentapi.rag.RagSearchService;

@Component
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseRagContextProvider implements RagContextProvider {

    private final RagProperties ragProperties;
    private final RagSearchService ragSearchService;
    private final AssistantService assistantService;

    public DatabaseRagContextProvider(
            RagProperties ragProperties,
            RagSearchService ragSearchService,
            AssistantService assistantService) {
        this.ragProperties = ragProperties;
        this.ragSearchService = ragSearchService;
        this.assistantService = assistantService;
    }

    @Override
    public Optional<String> retrievalContext(AgentContext context, String userMessage) {
        if (!ragProperties.isEnabled() || !ragProperties.isInjectIntoSystemPrompt()) {
            return Optional.empty();
        }
        Optional<Assistant> assistant = assistantService.findActiveByCode(context.getAssistantCode());
        if (assistant.isEmpty() || !assistant.get().ragInjectEnabled()) {
            return Optional.empty();
        }
        int topK = assistant.get().ragTopK() > 0 ? assistant.get().ragTopK() : ragProperties.getTopK();
        List<RagHit> hits = ragSearchService.search(userMessage, topK);
        if (hits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(RagContextFormatter.formatForPrompt(hits));
    }
}
