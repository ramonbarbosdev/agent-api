package com.agentapi.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.agentapi.assistant.AssistantCodes;
import com.agentapi.llm.LlmClient;
import com.agentapi.web.AgentChatHistoryMessage;

@SpringBootTest
@ActiveProfiles("test")
class ConversationPersistenceIntegrationTest {

    @Autowired
    private ConversationService conversationService;

    @MockitoBean
    private LlmClient llmClient;

    @Test
    void persistsAndReloadsHistory() {
        UUID id = UUID.randomUUID();
        conversationService.ensureConversation(id, AssistantCodes.HORAS_EXTRAS, null);
        conversationService.appendTurn(id, "Pergunta 1", "Resposta 1");

        List<AgentChatHistoryMessage> history = conversationService.loadHistoryForLlm(id);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getRole()).isEqualTo("user");
        assertThat(history.get(0).getContent()).isEqualTo("Pergunta 1");
        assertThat(history.get(1).getRole()).isEqualTo("assistant");
        assertThat(history.get(1).getContent()).isEqualTo("Resposta 1");

        var clientMessages = conversationService.loadMessagesForClient(id);
        assertThat(clientMessages).hasSize(2);
        assertThat(clientMessages.get(0).role()).isEqualTo("user");
        assertThat(clientMessages.get(1).content()).isEqualTo("Resposta 1");
    }
}
