package com.agentapi.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgentChatStreamRequest {

    @NotBlank
    @Size(max = 64)
    private String assistant;

    @NotBlank
    @Size(max = 4000)
    private String message;

    @Size(max = 64)
    private String conversationId;

    public String getAssistant() {
        return assistant;
    }

    public void setAssistant(String assistant) {
        this.assistant = assistant;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
