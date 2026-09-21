package com.agentapi.agent;

import org.springframework.stereotype.Component;

/**
 * Políticas de autorização e confirmação para ações do agente (futuro).
 */
@Component
public class AgentPolicy {

    public boolean requiresConfirmation(AgentContext context, String operationType) {
        return false;
    }
}
