package com.agentapi.tool;

import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;

@Component
public class ToolExecutor {

    /**
     * Execução de ferramentas será implementada quando o Agent Engine suportar tool calling.
     */
    public String execute(AgentContext context, String toolName, String arguments) {
        throw new UnsupportedOperationException("Tool execution is not implemented in the MVP.");
    }
}
