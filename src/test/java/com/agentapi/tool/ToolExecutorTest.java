package com.agentapi.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.agentapi.agent.AgentContext;
import com.agentapi.agent.AgentPolicy;
import com.agentapi.assistant.AssistantType;
import com.agentapi.exception.ToolException;
import com.agentapi.tool.horasextras.ConsultarPoliticaHorasExtrasTool;
import com.agentapi.tool.horasextras.RegistrarHorasExtrasTool;
import com.fasterxml.jackson.databind.ObjectMapper;

class ToolExecutorTest {

    private ToolRegistry toolRegistry;
    private ToolExecutor toolExecutor;

    @BeforeEach
    void setUp() {
        toolRegistry = new ToolRegistry();
        toolRegistry.register(new ConsultarPoliticaHorasExtrasTool());
        toolRegistry.register(new RegistrarHorasExtrasTool());
        toolExecutor = new ToolExecutor(toolRegistry, new AgentPolicy(), new ObjectMapper());
    }

    @Test
    void executesReadTool() {
        AgentContext context = AgentContext.builder().assistant(AssistantType.HORAS_EXTRAS).build();

        ToolResult result = toolExecutor.execute(context, "consultar_politica_horas_extras", "{}");

        assertThat(result.success()).isTrue();
        assertThat(result.content()).contains("politica");
    }

    @Test
    void blocksWriteTool() {
        AgentContext context = AgentContext.builder().assistant(AssistantType.HORAS_EXTRAS).build();

        assertThatThrownBy(() -> toolExecutor.execute(
                        context,
                        "registrar_horas_extras",
                        "{\"matricula\":\"1\",\"quantidadeHoras\":2,\"dataReferencia\":\"2026-01-01\"}"))
                .isInstanceOf(ToolException.class);
    }
}
