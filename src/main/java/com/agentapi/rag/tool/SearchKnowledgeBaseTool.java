package com.agentapi.rag.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.rag.RagContextFormatter;
import com.agentapi.rag.RagHit;
import com.agentapi.rag.RagSearchService;
import com.agentapi.tool.AgentTool;
import com.agentapi.tool.ToolKind;
import com.agentapi.tool.ToolResult;
import com.agentapi.tool.ToolSchemas;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SearchKnowledgeBaseTool implements AgentTool {

    private final RagSearchService ragSearchService;

    public SearchKnowledgeBaseTool(RagSearchService ragSearchService) {
        this.ragSearchService = ragSearchService;
    }

    @Override
    public String name() {
        return "search_knowledge_base";
    }

    @Override
    public String description() {
        return "Busca trechos na base de conhecimento (notas pessoais, projetos, politicas) antes de responder fatos especificos.";
    }

    @Override
    public ToolKind kind() {
        return ToolKind.READ;
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("query", ToolSchemas.stringProperty("Pergunta ou termos de busca"));
        return ToolSchemas.objectSchema(properties, List.of("query"));
    }

    @Override
    public ToolResult execute(AgentContext context, JsonNode arguments) {
        if (!arguments.hasNonNull("query") || arguments.get("query").asText().isBlank()) {
            return ToolResult.fail("Informe o parametro query.");
        }
        String query = arguments.get("query").asText().trim();
        List<RagHit> hits = ragSearchService.searchForChat(query);
        if (hits.isEmpty()) {
            return ToolResult.ok("{\"encontrados\":0,\"mensagem\":\"Nenhum trecho relevante na base.\"}");
        }
        String formatted = RagContextFormatter.formatForPrompt(hits);
        return ToolResult.ok("{\"encontrados\":" + hits.size() + ",\"trechos\":\"" + escape(formatted) + "\"}");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
