package com.agentapi.llm;

import java.util.List;

public record LlmResponse(String content, List<LlmToolCall> toolCalls) {

    public LlmResponse(String content) {
        this(content, List.of());
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
