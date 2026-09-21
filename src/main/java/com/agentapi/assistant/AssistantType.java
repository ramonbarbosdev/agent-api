package com.agentapi.assistant;

import java.util.Arrays;
import java.util.Optional;

/**
 * Tipos de assistente disponíveis na plataforma.
 * Futuros: FINANCEIRO, SUPORTE, COMERCIAL, RH, etc.
 */
public enum AssistantType {

    HORAS_EXTRAS;

    public static Optional<AssistantType> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(t -> t.name().equals(normalized))
                .findFirst();
    }
}
