package com.agentapi.web;

import java.util.Map;

import com.agentapi.tool.ToolKind;

public record ToolDescriptorDto(
        String name,
        String description,
        ToolKind kind,
        Map<String, Object> parametersSchema) {
}
