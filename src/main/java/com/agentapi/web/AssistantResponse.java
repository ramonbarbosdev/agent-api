package com.agentapi.web;

import java.util.List;

public record AssistantResponse(
        String code,
        String name,
        String description,
        String systemPrompt,
        String model,
        boolean active,
        boolean ragInject,
        int ragTopK,
        List<String> tools) {
}
