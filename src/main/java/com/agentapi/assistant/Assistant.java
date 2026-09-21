package com.agentapi.assistant;

public record Assistant(
        AssistantType type,
        String name,
        String description,
        String systemPrompt,
        String modelOverride) {

    public String resolveModel(String defaultModel) {
        return modelOverride != null && !modelOverride.isBlank() ? modelOverride : defaultModel;
    }
}
