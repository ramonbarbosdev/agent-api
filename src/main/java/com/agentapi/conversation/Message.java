package com.agentapi.conversation;

import java.time.Instant;

public record Message(String role, String content, Instant createdAt) {

    public static Message of(String role, String content) {
        return new Message(role, content, Instant.now());
    }
}
