package com.agentapi.web;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgentChatRequest {

    @NotBlank(message = "O assistente é obrigatório.")
    private String assistant;

    @NotBlank(message = "A mensagem é obrigatória.")
    @Size(max = 4000, message = "A mensagem deve ter no máximo 4000 caracteres.")
    private String message;

    /**
     * Thread opcional. Se omitido, a API cria um novo UUID e devolve na resposta.
     */
    @Size(max = 36, message = "conversationId inválido.")
    private String conversationId;

    @Valid
    @Size(max = 50, message = "O histórico deve ter no máximo 50 mensagens.")
    private List<AgentChatHistoryMessage> history = new ArrayList<>();

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

    public List<AgentChatHistoryMessage> getHistory() {
        return history;
    }

    public void setHistory(List<AgentChatHistoryMessage> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}
