package com.agentapi.web;

import java.util.List;

public record AgentPlatformStatusResponse(
        String apiStatus,
        boolean ready,
        LlmStatusDto llm,
        String activeAssistantId,
        List<AssistantStatusDto> assistants,
        List<StatusCheck> checks) {
}
