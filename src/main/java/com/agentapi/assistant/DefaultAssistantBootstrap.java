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
    private static final String HORAS_EXTRAS_PROMPT_PATH = "prompts/horas-extras-system.txt";

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
    public void seedDefaultAssistantIfEmpty() {
        if (assistenteRepository.count() > 0) {
            return;
        }
        log.info("Nenhum assistente no banco; criando padrão {}", AssistantCodes.HORAS_EXTRAS);

        String prompt = promptComposer.compose(
                promptLoader.load(BASE_PROMPT_PATH),
                promptLoader.load(HORAS_EXTRAS_PROMPT_PATH));

        UUID id = UUID.randomUUID();
        AssistenteEntity entity = new AssistenteEntity();
        entity.setIdAssistente(id);
        entity.setCdAssistente(AssistantCodes.HORAS_EXTRAS);
        entity.setNmNome("Assistente de Horas Extras");
        entity.setDsDescricao("Auxilia consultas e gestão de horas extras.");
        entity.setDsSystemPrompt(prompt);
        entity.setNmModelo(null);
        entity.setFlAtivo(true);
        entity.setFlRagInject(true);
        entity.setNuRagTopK(4);
        assistenteRepository.save(entity);

        for (String tool : List.of(
                "obter_data_hora_servidor",
                "consultar_politica_horas_extras",
                "search_knowledge_base",
                "registrar_horas_extras")) {
            assistenteToolRepository.save(new AssistenteToolEntity(UUID.randomUUID(), id, tool));
        }
    }
}
