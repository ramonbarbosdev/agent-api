package com.agentapi.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgentChatRequest {

    @NotBlank(message = "O assistente é obrigatório.")
    private String assistant;

    @NotBlank(message = "A mensagem é obrigatória.")
    @Size(max = 4000, message = "A mensagem deve ter no máximo 4000 caracteres.")
    private String message;

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
}
