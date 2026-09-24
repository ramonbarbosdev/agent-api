package com.agentapi.agent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.agentapi.exception.ApiException;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.exception.LlmException;
import com.agentapi.web.AgentChatResponse;
import com.agentapi.web.AgentChatStreamEvent;
import com.agentapi.web.AgentChatStreamRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
public class AgentChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentChatWebSocketHandler.class);

    private final AgentService agentService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public AgentChatWebSocketHandler(AgentService agentService, ObjectMapper objectMapper, Validator validator) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        AgentChatStreamRequest request;
        try {
            request = objectMapper.readValue(message.getPayload(), AgentChatStreamRequest.class);
        } catch (Exception ex) {
            send(session, AgentChatStreamEvent.error("JSON inválido.", "INVALID_REQUEST"));
            return;
        }

        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("Requisição inválida.");
            send(session, AgentChatStreamEvent.error(msg, "INVALID_REQUEST"));
            return;
        }

        AtomicBoolean busy = (AtomicBoolean) session.getAttributes().computeIfAbsent(
                "busy", key -> new AtomicBoolean(false));
        if (!busy.compareAndSet(false, true)) {
            send(session, AgentChatStreamEvent.error(
                    "Aguarde a resposta anterior terminar antes de enviar outra mensagem.",
                    "INVALID_REQUEST"));
            return;
        }

        try {
            AgentChatResponse response = agentService.chatStream(request, new AgentStreamEmitter() {
                @Override
                public void onPhase(String phaseMessage) {
                    try {
                        send(session, AgentChatStreamEvent.phase(phaseMessage));
                    } catch (IOException e) {
                        log.debug("WebSocket phase send failed: {}", e.getMessage());
                    }
                }

                @Override
                public void onToken(String token) {
                    try {
                        send(session, AgentChatStreamEvent.token(token));
                    } catch (IOException e) {
                        log.debug("WebSocket token send failed: {}", e.getMessage());
                    }
                }

                @Override
                public void onDone(String fullMessage) {
                    // conclusão enviada após persistência no serviço
                }
            });
            send(session, AgentChatStreamEvent.done(response.message(), response.conversationId()));
        } catch (AssistantNotFoundException ex) {
            send(session, AgentChatStreamEvent.error(ex.getMessage(), ex.getCode().name()));
        } catch (LlmException ex) {
            send(session, AgentChatStreamEvent.error(ex.getMessage(), ex.getCode().name()));
        } catch (ApiException ex) {
            send(session, AgentChatStreamEvent.error(ex.getMessage(), ex.getCode().name()));
        } catch (Exception ex) {
            log.error("WebSocket chat failed", ex);
            send(session, AgentChatStreamEvent.error(
                    "Não foi possível obter uma resposta do assistente.",
                    "INTERNAL_ERROR"));
        } finally {
            busy.set(false);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        session.getAttributes().remove("busy");
    }

    private void send(WebSocketSession session, AgentChatStreamEvent event) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(event)));
        }
    }
}
