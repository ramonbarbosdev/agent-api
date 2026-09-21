package com.agentapi.web;

public record AssistantStatusDto(
        String id,
        String name,
        String description,
        String model,
        boolean available) {
}
