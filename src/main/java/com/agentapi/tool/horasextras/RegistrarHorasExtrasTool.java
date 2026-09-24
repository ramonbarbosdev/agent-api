package com.agentapi.tool.horasextras;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.tool.AgentTool;
import com.agentapi.tool.ToolKind;
import com.agentapi.tool.ToolResult;
import com.agentapi.tool.ToolSchemas;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Operação de escrita — bloqueada por {@link com.agentapi.agent.AgentPolicy}
 * até confirmação do usuário.
 */
@Component
public class RegistrarHorasExtrasTool implements AgentTool {

    @Override
    public String name() {
        return "registrar_horas_extras";
    }

    @Override
    public String description() {
        return "Registra horas extras no sistema de ponto (requer confirmação; integração futura).";
    }

    @Override
    public ToolKind kind() {
        return ToolKind.WRITE;
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("matricula", ToolSchemas.stringProperty("Matrícula ou identificador do colaborador"));
        properties.put("quantidadeHoras", ToolSchemas.integerProperty("Quantidade de horas a registrar"));
        properties.put("dataReferencia", ToolSchemas.stringProperty("Data de referência (AAAA-MM-DD)"));
        return ToolSchemas.objectSchema(properties, List.of("matricula", "quantidadeHoras", "dataReferencia"));
    }

    @Override
    public ToolResult execute(AgentContext context, JsonNode arguments) {
        return ToolResult.ok("{\"status\":\"pendente_integracao\"}");
    }
}
