package com.agentapi.assistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteRepository;

/**
 * Mantém o prompt do assistente PERSONAL alinhado aos arquivos em {@code prompts/} a cada subida da API.
 */
@Component
public class PersonalAssistantPromptSync {

    private static final Logger log = LoggerFactory.getLogger(PersonalAssistantPromptSync.class);

    private static final String BASE_PROMPT_PATH = "prompts/base-system.txt";
    private static final String PERSONAL_PROMPT_PATH = "prompts/personal-system.txt";

    private final AssistenteRepository assistenteRepository;
    private final PromptLoader promptLoader;
    private final PromptComposer promptComposer;

    public PersonalAssistantPromptSync(
            AssistenteRepository assistenteRepository,
            PromptLoader promptLoader,
            PromptComposer promptComposer) {
        this.assistenteRepository = assistenteRepository;
        this.promptLoader = promptLoader;
        this.promptComposer = promptComposer;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(105)
    @Transactional
    public void syncFromClasspath() {
        assistenteRepository.findByCdAssistenteIgnoreCase(AssistantCodes.PERSONAL).ifPresent(entity -> {
            String prompt = promptComposer.compose(
                    promptLoader.load(BASE_PROMPT_PATH),
                    promptLoader.load(PERSONAL_PROMPT_PATH));
            entity.setDsSystemPrompt(prompt);
            assistenteRepository.save(entity);
            log.debug("Prompt {} espelhado no banco (runtime usa classpath via PersonalSystemPromptResolver)", AssistantCodes.PERSONAL);
        });
    }
}
