package com.agentapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.conversation")
public class ConversationProperties {

    /**
     * Máximo de mensagens anteriores enviadas ao LLM por turno.
     */
    private int historyLimit = 50;

    public int getHistoryLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(int historyLimit) {
        this.historyLimit = historyLimit;
    }
}
