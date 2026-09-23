package com.agentapi.conversation;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.AssistantType;
import com.agentapi.config.ConversationProperties;
import com.agentapi.conversation.persistence.ConversaEntity;
import com.agentapi.conversation.persistence.ConversaRepository;
import com.agentapi.conversation.persistence.MensagemConversaEntity;
import com.agentapi.conversation.persistence.MensagemConversaRepository;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.web.AgentChatHistoryMessage;

@Service
public class ConversationService {

    private final ConversaRepository conversaRepository;
    private final MensagemConversaRepository mensagemConversaRepository;
    private final ConversationProperties properties;

    public ConversationService(
            ConversaRepository conversaRepository,
            MensagemConversaRepository mensagemConversaRepository,
            ConversationProperties properties) {
        this.conversaRepository = conversaRepository;
        this.mensagemConversaRepository = mensagemConversaRepository;
        this.properties = properties;
    }

    public UUID resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(conversationId.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    ErrorCode.INVALID_REQUEST,
                    "conversationId inválido. Use um UUID (ex.: 550e8400-e29b-41d4-a716-446655440000).");
        }
    }

    @Transactional
    public void ensureConversation(UUID idConversa, AssistantType assistant, UUID idUsuario) {
        conversaRepository.findById(idConversa).ifPresentOrElse(
                existing -> {
                    if (!existing.getNmAssistente().equals(assistant.name())) {
                        throw new ApiException(
                                ErrorCode.INVALID_REQUEST,
                                "Esta conversa pertence ao assistente "
                                        + existing.getNmAssistente()
                                        + ". Inicie uma nova conversa para "
                                        + assistant.name()
                                        + ".");
                    }
                },
                () -> conversaRepository.save(new ConversaEntity(
                        idConversa,
                        assistant.name(),
                        idUsuario)));
    }

    @Transactional(readOnly = true)
    public List<AgentChatHistoryMessage> loadHistoryForLlm(UUID idConversa) {
        int limit = Math.max(1, properties.getHistoryLimit());
        List<MensagemConversaEntity> all =
                mensagemConversaRepository.findByIdConversaOrderByDtCriacaoAsc(idConversa);
        if (all.isEmpty()) {
            return List.of();
        }

        List<MensagemConversaEntity> window = all.size() <= limit
                ? all
                : all.subList(all.size() - limit, all.size());

        return window.stream().map(m -> {
            AgentChatHistoryMessage item = new AgentChatHistoryMessage();
            item.setRole(m.getTpPapel());
            item.setContent(m.getDsConteudo());
            return item;
        }).toList();
    }

    public List<AgentChatHistoryMessage> resolveHistoryForLlm(
            UUID idConversa,
            List<AgentChatHistoryMessage> clientHistory) {
        List<AgentChatHistoryMessage> persisted = loadHistoryForLlm(idConversa);
        if (!persisted.isEmpty()) {
            return persisted;
        }
        if (clientHistory == null || clientHistory.isEmpty()) {
            return List.of();
        }
        int limit = Math.max(1, properties.getHistoryLimit());
        if (clientHistory.size() <= limit) {
            return List.copyOf(clientHistory);
        }
        return List.copyOf(clientHistory.subList(clientHistory.size() - limit, clientHistory.size()));
    }

    @Transactional
    public void appendTurn(UUID idConversa, String userContent, String assistantContent) {
        mensagemConversaRepository.save(new MensagemConversaEntity(
                UUID.randomUUID(), idConversa, "user", userContent));
        mensagemConversaRepository.save(new MensagemConversaEntity(
                UUID.randomUUID(), idConversa, "assistant", assistantContent));
        conversaRepository.findById(idConversa).ifPresent(conversa -> {
            conversa.touch();
            conversaRepository.save(conversa);
        });
    }
}
