package com.agentapi.tool;

import java.util.Map;

public record ToolDefinitionDto(String name, String description, Map<String, Object> parameters) {
}
