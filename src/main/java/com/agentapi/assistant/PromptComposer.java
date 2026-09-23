package com.agentapi.assistant;

import org.springframework.stereotype.Component;

@Component
public class PromptComposer {

    private static final String SECTION_SEPARATOR = "\n\n---\n\n";

    /**
     * Concatena o prompt base da plataforma com o prompt específico do assistente.
     */
    public String compose(String basePrompt, String assistantPrompt) {
        if (basePrompt == null || basePrompt.isBlank()) {
            return assistantPrompt != null ? assistantPrompt.trim() : "";
        }
        if (assistantPrompt == null || assistantPrompt.isBlank()) {
            return basePrompt.trim();
        }
        return basePrompt.trim() + SECTION_SEPARATOR + assistantPrompt.trim();
    }
}
