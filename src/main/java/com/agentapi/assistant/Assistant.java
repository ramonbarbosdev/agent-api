package com.agentapi.assistant;

import java.util.UUID;

public record Assistant(
        UUID id,
        String code,
        String name,
        String description,
        String systemPrompt,
        String modelOverride,
        boolean active,
        boolean ragInjectEnabled,
        int ragTopK) {

    public String resolveModel(String defaultModel) {
        return modelOverride != null && !modelOverride.isBlank() ? modelOverride : defaultModel;
    }
}
