package com.agentapi.web;

import jakarta.validation.constraints.NotBlank;

public class ToolInvokeRequest {

    @NotBlank(message = "O assistente é obrigatório.")
    private String assistant;

    @NotBlank(message = "O nome da ferramenta é obrigatório.")
    private String tool;

    private String arguments = "{}";

    public String getAssistant() {
        return assistant;
    }

    public void setAssistant(String assistant) {
        this.assistant = assistant;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments != null ? arguments : "{}";
    }
}
