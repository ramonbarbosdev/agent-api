package com.agentapi.web;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentChatStreamEvent(
        String type,
        String content,
        String message,
        String conversationId,
        String code) {

    public static AgentChatStreamEvent token(String content) {
        return new AgentChatStreamEvent("token", content, null, null, null);
    }

    public static AgentChatStreamEvent phase(String message) {
        return new AgentChatStreamEvent("phase", null, message, null, null);
    }

    public static AgentChatStreamEvent done(String message, String conversationId) {
        return new AgentChatStreamEvent("done", null, message, conversationId, null);
    }

    public static AgentChatStreamEvent error(String message, String code) {
        return new AgentChatStreamEvent("error", null, message, null, code);
    }
}
