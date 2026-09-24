package com.agentapi.assistant;

import org.springframework.stereotype.Component;

import com.agentapi.assistant.persistence.AssistenteEntity;

@Component
class AssistantMapper {

    Assistant toDomain(AssistenteEntity entity) {
        return new Assistant(
                entity.getIdAssistente(),
                entity.getCdAssistente(),
                entity.getNmNome(),
                entity.getDsDescricao(),
                entity.getDsSystemPrompt(),
                entity.getNmModelo(),
                entity.isFlAtivo(),
                entity.isFlRagInject(),
                entity.getNuRagTopK());
    }
}
