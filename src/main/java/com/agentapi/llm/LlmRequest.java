package com.agentapi.llm;

import java.util.List;

public record LlmRequest(String model, List<LlmMessage> messages) {
}
