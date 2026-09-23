package com.agentapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent.rag")
public class RagProperties {

    private boolean enabled = true;

    private int topK = 4;

    /**
     * Incluir trechos automaticamente no system prompt a cada turno.
     */
    private boolean injectIntoSystemPrompt = true;

    private int chunkMaxChars = 900;

    private int chunkOverlapChars = 120;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public boolean isInjectIntoSystemPrompt() {
        return injectIntoSystemPrompt;
    }

    public void setInjectIntoSystemPrompt(boolean injectIntoSystemPrompt) {
        this.injectIntoSystemPrompt = injectIntoSystemPrompt;
    }

    public int getChunkMaxChars() {
        return chunkMaxChars;
    }

    public void setChunkMaxChars(int chunkMaxChars) {
        this.chunkMaxChars = chunkMaxChars;
    }

    public int getChunkOverlapChars() {
        return chunkOverlapChars;
    }

    public void setChunkOverlapChars(int chunkOverlapChars) {
        this.chunkOverlapChars = chunkOverlapChars;
    }
}
