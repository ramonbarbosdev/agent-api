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
 * Stub de leitura até integrar com API de RH/horas extras.
 */
@Component
public class ConsultarPoliticaHorasExtrasTool implements AgentTool {

    @Override
    public String name() {
        return "consultar_politica_horas_extras";
    }

    @Override
    public String description() {
        return "Consulta resumo da política interna de horas extras (limites e aprovação).";
    }

    @Override
    public ToolKind kind() {
        return ToolKind.READ;
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("topico", ToolSchemas.stringProperty(
                "Opcional: 'aprovacao', 'limites' ou 'compensacao'. Vazio retorna visão geral."));
        return ToolSchemas.objectSchema(properties, List.of());
    }

    @Override
    public ToolResult execute(AgentContext context, JsonNode arguments) {
        String topico = arguments.hasNonNull("topico") ? arguments.get("topico").asText().trim() : "geral";
        String texto = switch (topico.toLowerCase()) {
            case "aprovacao" -> "Horas extras exigem aprovação prévia do gestor imediato.";
            case "limites" -> "Limites mensais seguem a política interna da empresa (integração com sistema de ponto pendente).";
            case "compensacao" -> "Compensação em folga depende de acordo entre colaborador e gestor.";
            default -> "Política resumida: não registrar HE sem aprovação; não exceder limites internos; "
                    + "dados individuais de saldo virão da API de ponto (ainda não conectada).";
        };
        return ToolResult.ok("{\"topico\":\"" + topico + "\",\"resumo\":\"" + texto + "\",\"fonte\":\"politica_interna_stub\"}");
    }
}
