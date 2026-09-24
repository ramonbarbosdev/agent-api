package com.agentapi.assistant;

public final class AssistantCodes {

    public static final String PERSONAL = "PERSONAL";
    public static final String HORAS_EXTRAS = "HORAS_EXTRAS";

    private AssistantCodes() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    }
}
