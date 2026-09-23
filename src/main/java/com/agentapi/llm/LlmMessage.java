package com.agentapi.llm;

import java.util.List;

public record LlmMessage(String role, String content, List<LlmToolCall> toolCalls) {

    public LlmMessage {
        if (content == null) {
            content = "";
        }
    }

    public static LlmMessage system(String content) {
        return new LlmMessage("system", content, null);
    }

    public static LlmMessage user(String content) {
        return new LlmMessage("user", content, null);
    }

    public static LlmMessage assistant(String content) {
        return new LlmMessage("assistant", content, null);
    }

    public static LlmMessage assistantToolCalls(String content, List<LlmToolCall> toolCalls) {
        return new LlmMessage("assistant", content, toolCalls);
    }

    public static LlmMessage tool(String content) {
        return new LlmMessage("tool", content, null);
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
