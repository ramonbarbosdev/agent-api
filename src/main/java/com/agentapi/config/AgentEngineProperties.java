package com.agentapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.engine")
public class AgentEngineProperties {

    private int maxToolSteps = 5;

    /**
     * Limite aproximado de caracteres no contexto (system + histórico + user). Ajuste conforme o modelo.
     */
    private int maxContextChars = 48_000;

    public int getMaxToolSteps() {
        return maxToolSteps;
    }

    public void setMaxToolSteps(int maxToolSteps) {
        this.maxToolSteps = maxToolSteps;
    }

    public int getMaxContextChars() {
        return maxContextChars;
    }

    public void setMaxContextChars(int maxContextChars) {
        this.maxContextChars = maxContextChars;
    }
}
