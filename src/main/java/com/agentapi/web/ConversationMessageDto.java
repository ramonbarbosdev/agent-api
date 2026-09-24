package com.agentapi.web;

public record ConversationMessageDto(
        String id,
        String role,
        String content,
        String createdAt) {
}
