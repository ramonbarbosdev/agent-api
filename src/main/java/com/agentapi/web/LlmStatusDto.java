package com.agentapi.web;

public record LlmStatusDto(
        String provider,
        String baseUrl,
        String model,
        boolean reachable,
        boolean modelReady) {
}
