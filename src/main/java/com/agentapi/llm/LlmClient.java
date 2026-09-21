package com.agentapi.llm;

public interface LlmClient {

    LlmResponse chat(LlmRequest request);
}
