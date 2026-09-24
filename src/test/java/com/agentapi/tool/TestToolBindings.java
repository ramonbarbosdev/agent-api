package com.agentapi.tool;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

public final class TestToolBindings {

    private TestToolBindings() {
    }

    public static ToolRegistry registryWith(List<String> toolNames, AgentTool... tools) {
        AssistantToolBindingService bindingService = mock(AssistantToolBindingService.class);
        when(bindingService.toolNamesForAssistant(anyString())).thenReturn(toolNames);
        ToolRegistry registry = new ToolRegistry(bindingService);
        for (AgentTool tool : tools) {
            registry.register(tool);
        }
        return registry;
    }
}
