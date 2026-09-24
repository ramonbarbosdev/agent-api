package com.agentapi.config;

import java.util.List;

/**
 * Origens permitidas para CORS (REST) e WebSocket do agent-front.
 * Ajuste esta lista ao deploy (ex.: host Tailscale do front na VPS).
 */
public final class AgentCorsOrigins {

    private AgentCorsOrigins() {
    }

    public static final List<String> ALLOWED = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173");

    public static String[] asArray() {
        return ALLOWED.toArray(String[]::new);
    }
}
