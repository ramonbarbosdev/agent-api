package com.agentapi.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();
    private final AssistantToolBindingService bindingService;

    public ToolRegistry(AssistantToolBindingService bindingService) {
        this.bindingService = bindingService;
    }

    public void register(AgentTool tool) {
        tools.put(tool.name(), tool);
    }

    public Optional<AgentTool> getByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Collection<AgentTool> listAll() {
        return List.copyOf(tools.values());
    }

    public List<AgentTool> listForAssistant(String assistantCode) {
        List<String> allowed = bindingService.toolNamesForAssistant(assistantCode);
        return allowed.stream()
                .map(tools::get)
                .filter(tool -> tool != null)
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
