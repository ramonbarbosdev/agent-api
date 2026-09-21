package com.agentapi.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AgentChatHistoryMessage {

    @NotBlank(message = "O papel da mensagem é obrigatório.")
    @Pattern(regexp = "user|assistant", message = "O papel deve ser user ou assistant.")
    private String role;

    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    @Size(max = 4000, message = "O conteúdo deve ter no máximo 4000 caracteres.")
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
