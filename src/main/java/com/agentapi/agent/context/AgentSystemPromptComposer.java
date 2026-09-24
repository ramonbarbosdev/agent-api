package com.agentapi.agent.context;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.agentapi.agent.AgentContext;
import com.agentapi.assistant.Assistant;
import com.agentapi.assistant.PersonalSystemPromptResolver;
import com.agentapi.tool.ToolCatalogFormatter;

@Component
public class AgentSystemPromptComposer {

    private final ToolCatalogFormatter toolCatalogFormatter;
    private final ConversationMemoryProvider memoryProvider;
    private final RagContextProvider ragContextProvider;
    private final PersonalSystemPromptResolver personalSystemPromptResolver;

    public AgentSystemPromptComposer(
            ToolCatalogFormatter toolCatalogFormatter,
            ConversationMemoryProvider memoryProvider,
            RagContextProvider ragContextProvider,
            PersonalSystemPromptResolver personalSystemPromptResolver) {
        this.toolCatalogFormatter = toolCatalogFormatter;
        this.memoryProvider = memoryProvider;
        this.ragContextProvider = ragContextProvider;
        this.personalSystemPromptResolver = personalSystemPromptResolver;
    }

    public String compose(Assistant assistant, AgentContext context, String userMessage) {
        StringBuilder sb = new StringBuilder(personalSystemPromptResolver.effectiveSystemPrompt(assistant).trim());

        memoryProvider.longTermMemory(context).ifPresent(memory -> appendBlock(sb, "Memória da conversa", memory));

        Optional<String> ragContext = ragContextProvider.retrievalContext(context, userMessage);
        ragContext.ifPresent(rag -> {
            appendBlock(sb, "Base de conhecimento (trechos já recuperados para esta pergunta)", rag);
            appendBlock(
                    sb,
                    "Como responder agora",
                    "Use os trechos acima para responder a ultima mensagem do usuario. "
                            + "Nao repita meta-instrucoes nem peca ao usuario que consulte a base.");
        });

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
