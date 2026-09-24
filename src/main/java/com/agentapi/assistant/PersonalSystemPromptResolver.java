package com.agentapi.assistant;

import org.springframework.stereotype.Component;

/**
 * Assistente PERSONAL: system prompt sempre lido do classpath (prompts/*.txt), não do valor
 * desatualizado que pode ficar no banco após edições locais.
 */
@Component
public class PersonalSystemPromptResolver {

    private static final String BASE_PROMPT_PATH = "prompts/base-system.txt";
    private static final String PERSONAL_PROMPT_PATH = "prompts/personal-system.txt";

    private final PromptLoader promptLoader;
    private final PromptComposer promptComposer;

    public PersonalSystemPromptResolver(PromptLoader promptLoader, PromptComposer promptComposer) {
        this.promptLoader = promptLoader;
        this.promptComposer = promptComposer;
    }

    public String effectiveSystemPrompt(Assistant assistant) {
        if (!AssistantCodes.PERSONAL.equalsIgnoreCase(assistant.code())) {
            return assistant.systemPrompt();
        }
        return promptComposer.compose(
                promptLoader.load(BASE_PROMPT_PATH),
                promptLoader.load(PERSONAL_PROMPT_PATH));
    }
}
