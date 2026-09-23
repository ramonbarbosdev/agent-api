package com.agentapi.tool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistrar {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistrar.class);

    private final ToolRegistry toolRegistry;
    private final List<AgentTool> agentTools;

    public ToolRegistrar(ToolRegistry toolRegistry, List<AgentTool> agentTools) {
        this.toolRegistry = toolRegistry;
        this.agentTools = agentTools;
    }

    @EventListener(ApplicationReadyEvent.class)
    void registerTools() {
        for (AgentTool tool : agentTools) {
            toolRegistry.register(tool);
            log.info("Tool registered (name={}, kind={})", tool.name(), tool.kind());
        }
    }
}
