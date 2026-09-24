package com.agentapi.agent.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.PersonalSystemPromptResolver;
import com.agentapi.assistant.PromptComposer;
import com.agentapi.assistant.PromptLoader;
import com.agentapi.config.AgentEngineProperties;
import com.agentapi.config.OllamaProperties;
import com.agentapi.conversation.ConversationService;
import com.agentapi.tool.LlmToolDefinitionMapper;
import com.agentapi.tool.TestToolBindings;
import com.agentapi.tool.ToolCatalogFormatter;
import com.agentapi.tool.ToolRegistry;
import com.agentapi.tool.horasextras.ConsultarPoliticaHorasExtrasTool;
import com.agentapi.web.AgentChatHistoryMessage;

@ExtendWith(MockitoExtension.class)
class AgentTurnContextFactoryTest {

    @Mock
    private ConversationService conversationService;

    private AgentTurnContextFactory factory;

    @BeforeEach
    void setUp() {
        ToolRegistry registry = TestToolBindings.registryWith(
                List.of("consultar_politica_horas_extras"),
                new ConsultarPoliticaHorasExtrasTool());

        AgentEngineProperties engineProperties = new AgentEngineProperties();
        engineProperties.setMaxContextChars(800);

        OllamaProperties ollamaProperties = new OllamaProperties();
        ollamaProperties.setModel("test");

        factory = new AgentTurnContextFactory(
                conversationService,
                new AgentSystemPromptComposer(
                        new ToolCatalogFormatter(registry),
                        new NoOpConversationMemoryProvider(),
                        new NoOpRagContextProvider(),
                        new PersonalSystemPromptResolver(new PromptLoader(), new PromptComposer())),
                new LlmToolDefinitionMapper(registry),
                new ContextWindowTrimmer(),
                ollamaProperties,
                engineProperties);
    }

    @Test
    void buildsMessagesWithSystemHistoryAndUser() {
        UUID conversationId = UUID.randomUUID();
        when(conversationService.resolveHistoryForLlm(any(), any()))
                .thenReturn(List.of(userMessage("oi"), assistantMessage("olá")));

        Assistant assistant = new Assistant(
                UUID.randomUUID(),
                AssistantCodes.HORAS_EXTRAS,
                "HE",
                "desc",
                "prompt base",
                null,
                true,
                true,
                4);

        AgentContext context = AgentContext.builder()
                .assistantCode(AssistantCodes.HORAS_EXTRAS)
                .conversationId(conversationId)
                .build();

        AgentTurnContext turn = factory.build(context, assistant, "nova pergunta", List.of());

        assertThat(turn.messages()).hasSize(4);
        assertThat(turn.messages().get(0).role()).isEqualTo("system");
        assertThat(turn.messages().get(0).content()).contains("prompt base");
        assertThat(turn.messages().get(3).role()).isEqualTo("user");
        assertThat(turn.messages().get(3).content()).isEqualTo("nova pergunta");
        assertThat(turn.tools()).hasSize(1);
    }

    @Test
    void trimsOldHistoryWhenOverBudget() {
        when(conversationService.resolveHistoryForLlm(any(), any()))
                .thenReturn(List.of(
                        userMessage("x".repeat(200)),
                        assistantMessage("y".repeat(200)),
                        userMessage("z".repeat(200))));

        Assistant assistant = new Assistant(
                UUID.randomUUID(),
                AssistantCodes.HORAS_EXTRAS,
                "HE",
                "desc",
                "s".repeat(100),
                null,
                true,
                true,
                4);

        AgentContext context = AgentContext.builder()
                .assistantCode(AssistantCodes.HORAS_EXTRAS)
                .conversationId(UUID.randomUUID())
                .build();

        AgentTurnContext turn = factory.build(context, assistant, "pergunta", List.of());

        assertThat(turn.historyMessagesDropped()).isGreaterThan(0);
        assertThat(turn.historyMessagesIncluded()).isLessThan(3);
    }

    private static AgentChatHistoryMessage userMessage(String content) {
        AgentChatHistoryMessage message = new AgentChatHistoryMessage();
        message.setRole("user");
        message.setContent(content);
        return message;
    }

    private static AgentChatHistoryMessage assistantMessage(String content) {
        AgentChatHistoryMessage message = new AgentChatHistoryMessage();
        message.setRole("assistant");
        message.setContent(content);
        return message;
    }
}
