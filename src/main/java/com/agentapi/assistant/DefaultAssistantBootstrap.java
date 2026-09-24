package com.agentapi.assistant;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.assistant.persistence.AssistenteToolEntity;
import com.agentapi.assistant.persistence.AssistenteToolRepository;

@Component
public class DefaultAssistantBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DefaultAssistantBootstrap.class);

    private static final String BASE_PROMPT_PATH = "prompts/base-system.txt";
    private static final String PERSONAL_PROMPT_PATH = "prompts/personal-system.txt";

    private static final List<String> PERSONAL_TOOLS = List.of(
            "obter_data_hora_servidor",
            "search_knowledge_base");

    private final AssistenteRepository assistenteRepository;
    private final AssistenteToolRepository assistenteToolRepository;
    private final PromptLoader promptLoader;
    private final PromptComposer promptComposer;

    public DefaultAssistantBootstrap(
            AssistenteRepository assistenteRepository,
            AssistenteToolRepository assistenteToolRepository,
            PromptLoader promptLoader,
            PromptComposer promptComposer) {
        this.assistenteRepository = assistenteRepository;
        this.assistenteToolRepository = assistenteToolRepository;
        this.promptLoader = promptLoader;
        this.promptComposer = promptComposer;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(100)
    @Transactional
    public void ensurePersonalAssistant() {
        if (assistenteRepository.findByCdAssistenteIgnoreCase(AssistantCodes.PERSONAL).isPresent()) {
            return;
        }
        log.info("Assistente {} não encontrado; criando padrão pessoal", AssistantCodes.PERSONAL);

        String prompt = promptComposer.compose(
                promptLoader.load(BASE_PROMPT_PATH),
                promptLoader.load(PERSONAL_PROMPT_PATH));

        UUID id = UUID.randomUUID();
        AssistenteEntity entity = new AssistenteEntity();
        entity.setIdAssistente(id);
        entity.setCdAssistente(AssistantCodes.PERSONAL);
        entity.setNmNome("Assistente pessoal");
        entity.setDsDescricao("Assistente pessoal — conversa, RAG e ferramentas básicas.");
        entity.setDsSystemPrompt(prompt);
        entity.setNmModelo(null);
        entity.setFlAtivo(true);
        entity.setFlRagInject(true);
        entity.setNuRagTopK(6);
        assistenteRepository.save(entity);

        for (String tool : PERSONAL_TOOLS) {
            assistenteToolRepository.save(new AssistenteToolEntity(UUID.randomUUID(), id, tool));
        }
    }
}
