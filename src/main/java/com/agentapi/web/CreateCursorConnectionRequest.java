package com.agentapi.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCursorConnectionRequest {

    @NotBlank
    @Size(max = 512)
    private String apiKey;

    @Size(max = 128)
    private String label;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
