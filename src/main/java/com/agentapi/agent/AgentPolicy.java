package com.agentapi.agent;

import org.springframework.stereotype.Component;

import com.agentapi.tool.AgentTool;
import com.agentapi.tool.ToolKind;

/**
 * Políticas de autorização e confirmação para ações do agente.
 */
@Component
public class AgentPolicy {

    public boolean requiresConfirmation(AgentContext context, String operationType) {
        return false;
    }

    public boolean canExecuteTool(AgentContext context, AgentTool tool) {
        if (tool.kind() == ToolKind.READ) {
            return true;
        }
        // WRITE: bloqueado até fluxo de confirmação (fase 4+)
        return false;
    }
}
