package com.agentapi.agent.context;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DatabaseRagContextProvider.class);

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
        if (!ragProperties.isEnabled()) {
            return Optional.empty();
        }
        if (!ragProperties.isInjectIntoSystemPrompt()) {
            log.debug(
                    "RAG inject disabled globally (agent.rag.inject-into-system-prompt / AGENT_RAG_INJECT=false)");
            return Optional.empty();
        }
        Optional<Assistant> assistant = assistantService.findActiveByCode(context.getAssistantCode());
        if (assistant.isEmpty() || !assistant.get().ragInjectEnabled()) {
            return Optional.empty();
        }
        int topK = assistant.get().ragTopK() > 0 ? assistant.get().ragTopK() : ragProperties.getTopK();
        List<RagHit> hits = ragSearchService.search(userMessage, topK);
        if (hits.isEmpty()) {
            log.info("RAG inject skipped (assistant={}, no hits for user message)", context.getAssistantCode());
            return Optional.empty();
        }
        log.info(
                "RAG inject (assistant={}, hits={}, firstTitle={})",
                context.getAssistantCode(),
                hits.size(),
                hits.get(0).titulo());
        return Optional.of(RagContextFormatter.formatForPrompt(hits));
    }
}
