package com.agentapi.llm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.agentapi.config.OllamaProperties;
import com.agentapi.exception.ErrorCode;
import com.agentapi.exception.LlmException;

public class OllamaClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final RestClient restClient;
    private final OllamaProperties properties;

    public OllamaClient(RestClient restClient, OllamaProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        String model = request.model() != null ? request.model() : properties.getModel();
        OllamaChatRequest body = new OllamaChatRequest(model, toOllamaMessages(request.messages()), false);

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

            if (response == null || response.message == null || response.message.content == null) {
                throw LlmException.of(ErrorCode.LLM_COMMUNICATION_ERROR,
                        "Resposta inválida do serviço de inteligência artificial.");
            }
            return new LlmResponse(response.message.content);
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

    private static List<OllamaMessage> toOllamaMessages(List<LlmMessage> messages) {
        return messages.stream()
                .map(m -> new OllamaMessage(m.role(), m.content()))
                .toList();
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

    private record OllamaChatRequest(String model, List<OllamaMessage> messages, boolean stream) {
    }

    private record OllamaMessage(String role, String content) {
    }

    private static class OllamaChatResponse {
        public OllamaMessage message;
    }
}
