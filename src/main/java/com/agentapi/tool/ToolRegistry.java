package com.agentapi.tool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public void register(AgentTool tool) {
        tools.put(tool.name(), tool);
    }

    public Optional<AgentTool> getByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
