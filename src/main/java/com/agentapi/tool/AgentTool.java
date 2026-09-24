package com.agentapi.tool;

import java.util.Map;

import com.agentapi.agent.AgentContext;
import com.fasterxml.jackson.databind.JsonNode;

public interface AgentTool {

    String name();

    String description();

    ToolKind kind();

    Map<String, Object> parametersSchema();

    ToolResult execute(AgentContext context, JsonNode arguments);
}
