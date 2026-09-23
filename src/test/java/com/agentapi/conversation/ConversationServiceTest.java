package com.agentapi.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agentapi.assistant.AssistantType;
import com.agentapi.config.ConversationProperties;
import com.agentapi.conversation.persistence.ConversaEntity;
import com.agentapi.conversation.persistence.ConversaRepository;
import com.agentapi.conversation.persistence.MensagemConversaRepository;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;
import com.agentapi.web.AgentChatHistoryMessage;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversaRepository conversaRepository;

    @Mock
    private MensagemConversaRepository mensagemConversaRepository;

    private ConversationService service;

    @BeforeEach
    void setUp() {
        ConversationProperties properties = new ConversationProperties();
        properties.setHistoryLimit(50);
        service = new ConversationService(conversaRepository, mensagemConversaRepository, properties);
    }

    @Test
    void generatesUuidWhenConversationIdMissing() {
        UUID id = service.resolveConversationId(null);
        assertThat(id).isNotNull();
    }

    @Test
    void parsesValidConversationId() {
        UUID expected = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        assertThat(service.resolveConversationId("550e8400-e29b-41d4-a716-446655440000")).isEqualTo(expected);
    }

    @Test
    void rejectsInvalidConversationId() {
        assertThatThrownBy(() -> service.resolveConversationId("not-a-uuid"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
    }

    @Test
    void ensureConversationCreatesWhenMissing() {
        UUID id = UUID.randomUUID();
        when(conversaRepository.findById(id)).thenReturn(Optional.empty());
        when(conversaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.ensureConversation(id, AssistantType.HORAS_EXTRAS, null);

        verify(conversaRepository).save(any(ConversaEntity.class));
    }

    @Test
    void ensureConversationRejectsAssistantMismatch() {
        UUID id = UUID.randomUUID();
        when(conversaRepository.findById(id))
                .thenReturn(Optional.of(new ConversaEntity(id, "FINANCEIRO", null)));

        assertThatThrownBy(() -> service.ensureConversation(id, AssistantType.HORAS_EXTRAS, null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void ensureConversationSkipsInsertWhenExists() {
        UUID id = UUID.randomUUID();
        when(conversaRepository.findById(id))
                .thenReturn(Optional.of(new ConversaEntity(id, "HORAS_EXTRAS", null)));

        service.ensureConversation(id, AssistantType.HORAS_EXTRAS, null);

        verify(conversaRepository, never()).save(any());
    }

    @Test
    void resolveHistoryUsesClientWhenDatabaseEmpty() {
        UUID id = UUID.randomUUID();
        when(mensagemConversaRepository.findByIdConversaOrderByDtCriacaoAsc(id)).thenReturn(List.of());

        AgentChatHistoryMessage client = new AgentChatHistoryMessage();
        client.setRole("user");
        client.setContent("oi");

        List<AgentChatHistoryMessage> history = service.resolveHistoryForLlm(id, List.of(client));

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getContent()).isEqualTo("oi");
    }
}
