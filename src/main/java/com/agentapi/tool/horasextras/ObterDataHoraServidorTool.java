package com.agentapi.tool.horasextras;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.tool.AgentTool;
import com.agentapi.tool.ToolKind;
import com.agentapi.tool.ToolResult;
import com.agentapi.tool.ToolSchemas;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ObterDataHoraServidorTool implements AgentTool {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    @Override
    public String name() {
        return "obter_data_hora_servidor";
    }

    @Override
    public String description() {
        return "Retorna data e hora atuais do servidor (fuso America/Sao_Paulo) para referência em consultas.";
    }

    @Override
    public ToolKind kind() {
        return ToolKind.READ;
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return ToolSchemas.objectSchema(Map.of(), List.of());
    }

    @Override
    public ToolResult execute(AgentContext context, JsonNode arguments) {
        ZonedDateTime now = ZonedDateTime.now(ZONE);
        return ToolResult.ok("{\"dataHora\":\"" + now + "\",\"fuso\":\"America/Sao_Paulo\"}");
    }
}
