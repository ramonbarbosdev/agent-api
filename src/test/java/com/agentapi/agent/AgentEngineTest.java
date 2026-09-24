package com.agentapi.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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

import com.agentapi.agent.context.AgentTurnContext;
import com.agentapi.agent.context.AgentTurnContextFactory;
import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.AssistantService;
import com.agentapi.config.AgentEngineProperties;
import com.agentapi.llm.LlmClient;
import com.agentapi.llm.LlmMessage;
import com.agentapi.llm.LlmResponse;
import com.agentapi.llm.LlmToolCall;
import com.agentapi.tool.ToolExecutor;
import com.agentapi.tool.ToolResult;

@ExtendWith(MockitoExtension.class)
class AgentEngineTest {

    @Mock
    private AssistantService assistantService;

    @Mock
    private LlmClient llmClient;

    @Mock
    private ToolExecutor toolExecutor;

    @Mock
    private AgentTurnContextFactory turnContextFactory;

    private AgentEngine agentEngine;

    @BeforeEach
    void setUp() {
        AgentEngineProperties engineProperties = new AgentEngineProperties();
        engineProperties.setMaxToolSteps(5);

        agentEngine = new AgentEngine(
                assistantService,
                llmClient,
                toolExecutor,
                turnContextFactory,
                engineProperties);

        Assistant assistant = new Assistant(
                UUID.randomUUID(),
                AssistantCodes.HORAS_EXTRAS,
                "HE",
                "desc",
                "system",
                null,
                true,
                true,
                4);
        when(assistantService.findActiveByCode(AssistantCodes.HORAS_EXTRAS)).thenReturn(Optional.of(assistant));
    }

    @Test
    void runsToolLoopAndReturnsFinalAnswer() {
        AgentTurnContext turn = new AgentTurnContext(
                "test-model",
                List.of(LlmMessage.system("system"), LlmMessage.user("pergunta")),
                List.of(),
                0,
                0);
        when(turnContextFactory.build(any(), any(), any(), any())).thenReturn(turn);

        when(llmClient.chat(any()))
                .thenReturn(new LlmResponse(
                        "",
                        List.of(new LlmToolCall("consultar_politica_horas_extras", "{}"))))
                .thenReturn(new LlmResponse("Com base na política, é necessária aprovação.", List.of()));

        when(toolExecutor.execute(any(), any(), any()))
                .thenReturn(ToolResult.ok("{\"resumo\":\"aprovacao obrigatoria\"}"));

        AgentContext context = AgentContext.builder()
                .assistantCode(AssistantCodes.HORAS_EXTRAS)
                .conversationId(UUID.randomUUID())
                .build();

        String reply = agentEngine.run(context, "Qual a regra de aprovação?", List.of());

        assertThat(reply).contains("aprovação");
        verify(llmClient, times(2)).chat(any());
        verify(toolExecutor).execute(any(), org.mockito.ArgumentMatchers.eq("consultar_politica_horas_extras"), any());
    }
}
