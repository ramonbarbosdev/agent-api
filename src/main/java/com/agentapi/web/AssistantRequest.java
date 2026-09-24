package com.agentapi.web;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssistantRequest {

    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 512)
    private String description;

    @NotBlank
    @Size(max = 100_000)
    private String systemPrompt;

    private boolean prependBasePrompt = true;

    @Size(max = 128)
    private String model;

    private boolean active = true;

    private boolean ragInject = true;

    private int ragTopK = 4;

    private List<String> tools = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isPrependBasePrompt() {
        return prependBasePrompt;
    }

    public void setPrependBasePrompt(boolean prependBasePrompt) {
        this.prependBasePrompt = prependBasePrompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isRagInject() {
        return ragInject;
    }

    public void setRagInject(boolean ragInject) {
        this.ragInject = ragInject;
    }

    public int getRagTopK() {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK) {
        this.ragTopK = ragTopK;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools != null ? tools : new ArrayList<>();
    }
}
