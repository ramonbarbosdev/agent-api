package com.agentapi.web;

public record StatusCheck(
        String id,
        String level,
        String message,
        String hint) {
}
