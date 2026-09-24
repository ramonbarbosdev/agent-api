package com.agentapi.llm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.agentapi.config.OllamaProperties;
import com.agentapi.exception.ErrorCode;
import com.agentapi.exception.LlmException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;

    public OllamaClient(RestClient restClient, OllamaProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        String model = request.model() != null ? request.model() : properties.getModel();
        List<OllamaTool> tools = toOllamaTools(request.tools());
        OllamaChatRequest body = new OllamaChatRequest(
                model,
                toOllamaMessages(request.messages()),
                tools.isEmpty() ? null : tools,
                false,
                Map.of("temperature", properties.getTemperature()));

        try {
            OllamaChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String responseBody = readBody(res);
                        if (res.getStatusCode().value() == 404 || indicatesModelNotFound(responseBody)) {
                            throw LlmException.of(ErrorCode.MODEL_NOT_FOUND,
                                    "O modelo de inteligência artificial configurado não está disponível.");
                        }
                        throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                                "Não foi possível comunicar com o serviço de inteligência artificial.");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                                "O serviço de inteligência artificial retornou um erro.");
                    })
                    .body(OllamaChatResponse.class);

            if (response == null || response.message == null) {
                throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                        "Resposta inválida do serviço de inteligência artificial.");
            }

            String content = response.message.content != null ? response.message.content : "";
            List<LlmToolCall> toolCalls = mapToolCalls(response.message.toolCalls);

            if (content.isBlank() && toolCalls.isEmpty()) {
                throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                        "Resposta vazia do serviço de inteligência artificial.");
            }

            return new LlmResponse(content, toolCalls);
        } catch (LlmException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (isTimeout(e)) {
                log.warn("Ollama request timed out: {}", e.getMessage());
                throw LlmException.of(ErrorCode.LLM_TIMEOUT,
                        "O serviço de inteligência artificial demorou para responder.", e);
            }
            log.warn("Ollama unavailable: {}", e.getMessage());
            throw LlmException.of(ErrorCode.LLM_UNAVAILABLE,
                    "O serviço de inteligência artificial está temporariamente indisponível.", e);
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 404 || indicatesModelNotFound(responseBody)) {
                throw LlmException.of(ErrorCode.MODEL_NOT_FOUND,
                        "O modelo de inteligência artificial configurado não está disponível.", e);
            }
            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                    "Não foi possível comunicar com o serviço de inteligência artificial.", e);
        }
    }

    @Override
    public LlmResponse chatStream(LlmRequest request, Consumer<String> onToken) {
        if (request.tools() != null && !request.tools().isEmpty()) {
            LlmResponse response = chat(request);
            String content = response.content();
            if (content != null && !content.isEmpty()) {
                onToken.accept(content);
            }
            return response;
        }

        String model = request.model() != null ? request.model() : properties.getModel();
        OllamaChatRequest body = new OllamaChatRequest(
                model,
                toOllamaMessages(request.messages()),
                null,
                true,
                Map.of("temperature", properties.getTemperature()));

        try {
            return restClient.post()
                    .uri("/api/chat")
                    .body(body)
                    .exchange((req, res) -> {
                        if (res.getStatusCode().is4xxClientError()) {
                            String responseBody = readBody(res);
                            if (res.getStatusCode().value() == 404 || indicatesModelNotFound(responseBody)) {
                                throw LlmException.of(ErrorCode.MODEL_NOT_FOUND,
                                        "O modelo de inteligência artificial configurado não está disponível.");
                            }
                            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                                    "Não foi possível comunicar com o serviço de inteligência artificial.");
                        }
                        if (res.getStatusCode().is5xxServerError()) {
                            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                                    "O serviço de inteligência artificial retornou um erro.");
                        }

                        StringBuilder full = new StringBuilder();
                        List<OllamaToolCall> lastToolCalls = null;

                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(res.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) {
                                    continue;
                                }
                                OllamaStreamChunk chunk = objectMapper.readValue(line, OllamaStreamChunk.class);
                                if (chunk.message == null) {
                                    continue;
                                }
                                if (chunk.message.content != null && !chunk.message.content.isEmpty()) {
                                    onToken.accept(chunk.message.content);
                                    full.append(chunk.message.content);
                                }
                                if (chunk.message.toolCalls != null && !chunk.message.toolCalls.isEmpty()) {
                                    lastToolCalls = chunk.message.toolCalls;
                                }
                            }
                        }

                        String content = full.toString();
                        List<LlmToolCall> toolCalls = mapToolCalls(lastToolCalls);
                        if (content.isBlank() && toolCalls.isEmpty()) {
                            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                                    "Resposta vazia do serviço de inteligência artificial.");
                        }
                        return new LlmResponse(content, toolCalls);
                    });
        } catch (LlmException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (isTimeout(e)) {
                log.warn("Ollama stream timed out: {}", e.getMessage());
                throw LlmException.of(ErrorCode.LLM_TIMEOUT,
                        "O serviço de inteligência artificial demorou para responder.", e);
            }
            log.warn("Ollama unavailable: {}", e.getMessage());
            throw LlmException.of(ErrorCode.LLM_UNAVAILABLE,
                    "O serviço de inteligência artificial está temporariamente indisponível.", e);
        } catch (RestClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 404 || indicatesModelNotFound(responseBody)) {
                throw LlmException.of(ErrorCode.MODEL_NOT_FOUND,
                        "O modelo de inteligência artificial configurado não está disponível.", e);
            }
            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                    "Não foi possível comunicar com o serviço de inteligência artificial.", e);
        } catch (Exception e) {
            throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                    "Não foi possível comunicar com o serviço de inteligência artificial.", e);
        }
    }

    private static List<OllamaTool> toOllamaTools(List<LlmToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return List.of();
        }
        return tools.stream()
                .map(tool -> new OllamaTool("function", new OllamaToolFunction(
                        tool.name(),
                        tool.description(),
                        tool.parameters())))
                .toList();
    }

    private static List<OllamaMessage> toOllamaMessages(List<LlmMessage> messages) {
        return messages.stream().map(OllamaClient::toOllamaMessage).toList();
    }

    private static OllamaMessage toOllamaMessage(LlmMessage message) {
        List<OllamaToolCall> ollamaToolCalls = null;
        if (message.hasToolCalls()) {
            ollamaToolCalls = message.toolCalls().stream()
                    .map(call -> {
                        OllamaToolCall mapped = new OllamaToolCall();
                        mapped.function = new OllamaFunctionCall(call.name(), call.argumentsJson());
                        return mapped;
                    })
                    .toList();
        }
        return new OllamaMessage(message.role(), message.content(), ollamaToolCalls);
    }

    private static List<LlmToolCall> mapToolCalls(List<OllamaToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return List.of();
        }
        List<LlmToolCall> mapped = new ArrayList<>();
        for (OllamaToolCall call : toolCalls) {
            if (call == null || call.function == null || call.function.name == null) {
                continue;
            }
            String args = call.function.arguments != null ? call.function.arguments : "{}";
            mapped.add(new LlmToolCall(call.function.name, args));
        }
        return mapped;
    }

    private static boolean indicatesModelNotFound(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        String lower = body.toLowerCase();
        return lower.contains("model") && (lower.contains("not found") || lower.contains("does not exist"));
    }

    private static boolean isTimeout(ResourceAccessException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof java.net.SocketTimeoutException) {
                return true;
            }
            String name = cause.getClass().getSimpleName();
            if (name.contains("Timeout")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static String readBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes());
        } catch (Exception e) {
            return "";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaChatRequest(
            String model,
            List<OllamaMessage> messages,
            List<OllamaTool> tools,
            boolean stream,
            Map<String, Object> options) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaMessage(
            String role,
            String content,
            @JsonProperty("tool_calls") List<OllamaToolCall> toolCalls) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaTool(String type, OllamaToolFunction function) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaToolFunction(String name, String description, Map<String, Object> parameters) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class OllamaToolCall {
        public OllamaFunctionCall function;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record OllamaFunctionCall(String name, String arguments) {
    }

    private static class OllamaChatResponse {
        public OllamaResponseMessage message;
    }

    private static class OllamaStreamChunk {
        public OllamaResponseMessage message;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class OllamaResponseMessage {
        public String role;
        public String content;
        @JsonProperty("tool_calls")
        public List<OllamaToolCall> toolCalls;
    }
}
