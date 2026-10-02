package com.agentapi.web;

public record GatewaySessionResponse(
        String id,
        String status,
        String conversationId,
        String cursorAgentId) {
}
