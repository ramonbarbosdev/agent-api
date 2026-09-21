package com.agentapi.llm;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class OllamaProbe {

    private final RestClient restClient;

    public OllamaProbe(@Qualifier("ollamaProbeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Optional<List<String>> fetchModelNames() {
        try {
            OllamaTagsResponse response = restClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .body(OllamaTagsResponse.class);
            if (response == null || response.models == null) {
                return Optional.of(Collections.emptyList());
            }
            List<String> names = response.models.stream()
                    .map(model -> model.name)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.toList());
            return Optional.of(names);
        } catch (ResourceAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static boolean isModelAvailable(String configuredModel, List<String> installedModels) {
        if (configuredModel == null || configuredModel.isBlank()) {
            return false;
        }
        if (installedModels == null || installedModels.isEmpty()) {
            return false;
        }
        String target = configuredModel.trim();
        for (String installed : installedModels) {
            if (installed.equals(target)) {
                return true;
            }
            if (installed.startsWith(target + ":")) {
                return true;
            }
            if (target.contains(":") && installed.equals(target.split(":")[0])) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaTagsResponse {
        public List<OllamaModelRef> models;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OllamaModelRef {
        public String name;
    }
}
