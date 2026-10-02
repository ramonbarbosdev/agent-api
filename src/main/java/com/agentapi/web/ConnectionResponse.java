package com.agentapi.web;

public record ConnectionResponse(
        String id,
        String provider,
        String label,
        boolean active,
        String lastValidatedAt) {
}
