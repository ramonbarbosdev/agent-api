package com.agentapi.gateway.sidecar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.agentapi.config.GatewayProperties;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CursorSidecarClient {

    private static final Logger log = LoggerFactory.getLogger(CursorSidecarClient.class);

    private final RestClient restClient;
    private final GatewayProperties gatewayProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public CursorSidecarClient(GatewayProperties gatewayProperties, ObjectMapper objectMapper) {
        this.gatewayProperties = gatewayProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(gatewayProperties.getSidecarUrl())
                .defaultHeader("X-Sidecar-Token", gatewayProperties.getSidecarToken())
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean testApiKey(String apiKey) {
        try {
            JsonNode body = restClient.post()
                    .uri("/v1/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("apiKey", apiKey))
                    .retrieve()
                    .body(JsonNode.class);
            return body != null && body.path("ok").asBoolean(false);
        } catch (RestClientResponseException ex) {
            log.warn("Sidecar validate failed: {}", ex.getStatusCode());
            return false;
        } catch (Exception ex) {
            log.warn("Sidecar unreachable: {}", ex.getMessage());
            return false;
        }
    }

    public String createSession(String sessionId, String apiKey, String projectPath, String systemPrompt) {
        try {
            JsonNode body = restClient.post()
                    .uri("/v1/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sessionId", sessionId,
                            "apiKey", apiKey,
                            "projectPath", projectPath,
                            "systemPrompt", systemPrompt != null ? systemPrompt : ""))
                    .retrieve()
                    .body(JsonNode.class);
            if (body == null || !body.has("cursorAgentId")) {
                throw new ApiException(ErrorCode.AGENT_START_FAILED, "Resposta inválida do sidecar.");
            }
            return body.get("cursorAgentId").asText();
        } catch (RestClientResponseException ex) {
            throw new ApiException(ErrorCode.AGENT_START_FAILED, "Falha ao iniciar agente Cursor.");
        }
    }

    public void sendMessage(String sessionId, String message, Consumer<String> onToken) {
        try {
            restClient.post()
                    .uri("/v1/sessions/{id}/messages", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", message))
                    .retrieve()
                    .toBodilessEntity();
            streamSession(sessionId, onToken);
        } catch (RestClientResponseException ex) {
            throw new ApiException(ErrorCode.AGENT_ERROR, "Falha ao enviar mensagem ao Cursor.");
        }
    }

    private void streamSession(String sessionId, Consumer<String> onToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gatewayProperties.getSidecarUrl() + "/v1/sessions/" + sessionId + "/stream"))
                    .header("X-Sidecar-Token", gatewayProperties.getSidecarToken())
                    .header("Accept", "text/event-stream")
                    .GET()
                    .build();
            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (var reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        String json = line.substring(5).trim();
                        if (json.isEmpty() || "[DONE]".equals(json)) {
                            continue;
                        }
                        JsonNode node = objectMapper.readTree(json);
                        if ("agent.message".equals(node.path("type").asText()) && node.has("content")) {
                            onToken.accept(node.get("content").asText());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.AGENT_ERROR, "Falha no streaming do Cursor.");
        }
    }

    public boolean isHealthy() {
        try {
            JsonNode body = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(JsonNode.class);
            return body != null && body.path("status").asText("").equalsIgnoreCase("UP");
        } catch (Exception ex) {
            return false;
        }
    }
}
