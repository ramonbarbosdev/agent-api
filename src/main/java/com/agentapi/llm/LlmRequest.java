package com.agentapi.llm;

import java.util.List;

public record LlmRequest(String model, List<LlmMessage> messages, List<LlmToolDefinition> tools) {

    public LlmRequest(String model, List<LlmMessage> messages) {
        this(model, messages, List.of());
    }
}
