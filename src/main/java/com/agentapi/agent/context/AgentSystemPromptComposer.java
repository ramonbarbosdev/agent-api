package com.agentapi.agent.context;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.Assistant;
import com.agentapi.tool.ToolCatalogFormatter;

@Component
public class AgentSystemPromptComposer {

    private final ToolCatalogFormatter toolCatalogFormatter;
    private final ConversationMemoryProvider memoryProvider;
    private final RagContextProvider ragContextProvider;

    public AgentSystemPromptComposer(
            ToolCatalogFormatter toolCatalogFormatter,
            ConversationMemoryProvider memoryProvider,
            RagContextProvider ragContextProvider) {
        this.toolCatalogFormatter = toolCatalogFormatter;
        this.memoryProvider = memoryProvider;
        this.ragContextProvider = ragContextProvider;
    }

    public String compose(Assistant assistant, AgentContext context, String userMessage) {
        StringBuilder sb = new StringBuilder(assistant.systemPrompt().trim());

        memoryProvider.longTermMemory(context).ifPresent(memory -> appendBlock(sb, "Memória da conversa", memory));

        Optional<String> ragContext = ragContextProvider.retrievalContext(context, userMessage);
        ragContext.ifPresent(rag ->
                appendBlock(sb, "Base de conhecimento (trechos já recuperados para esta pergunta)", rag));

        String catalog = toolCatalogFormatter.formatForAssistant(assistant.code());
        if (!catalog.isBlank()) {
            appendBlock(sb, "Ferramentas", catalog);
        } else if (assistant.ragInjectEnabled()) {
            String noTools = ragContext.isPresent()
                    ? "Nenhuma. Os trechos da base de conhecimento acima já foram recuperados; "
                            + "responda com eles. Não mencione search_knowledge_base nem outras ferramentas."
                    : "Nenhuma. Se a base de conhecimento acima estiver vazia, diga que não encontrou na base. "
                            + "Não mencione search_knowledge_base.";
            appendBlock(sb, "Ferramentas", noTools);
        }

        return sb.toString();
    }

    private static void appendBlock(StringBuilder sb, String title, String body) {
        sb.append("\n\n---\n\n");
        sb.append("## ").append(title).append("\n\n");
        sb.append(body.trim());
    }
}
