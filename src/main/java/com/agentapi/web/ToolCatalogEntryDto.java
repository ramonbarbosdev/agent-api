package com.agentapi.web;

import com.agentapi.tool.ToolKind;

public record ToolCatalogEntryDto(String name, String description, ToolKind kind) {
}
