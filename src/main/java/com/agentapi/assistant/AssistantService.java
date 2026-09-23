package com.agentapi.assistant;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private static final String BASE_PROMPT_PATH = "prompts/base-system.txt";
    private static final String HORAS_EXTRAS_PROMPT_PATH = "prompts/horas-extras-system.txt";

    private final Map<AssistantType, Assistant> assistants = new EnumMap<>(AssistantType.class);

    public AssistantService(PromptLoader promptLoader, PromptComposer promptComposer) {
        String basePrompt = promptLoader.load(BASE_PROMPT_PATH);
        String horasExtrasDomain = promptLoader.load(HORAS_EXTRAS_PROMPT_PATH);
        String horasExtrasPrompt = promptComposer.compose(basePrompt, horasExtrasDomain);

        assistants.put(AssistantType.HORAS_EXTRAS, new Assistant(
                AssistantType.HORAS_EXTRAS,
                "Assistente de Horas Extras",
                "Auxilia consultas e gestão de horas extras.",
                horasExtrasPrompt,
                null));
    }

    public Optional<Assistant> find(AssistantType type) {
        return Optional.ofNullable(assistants.get(type));
    }

    public List<Assistant> listAll() {
        return new ArrayList<>(assistants.values());
    }
}
