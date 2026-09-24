package com.agentapi.tool;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.AssistantCodes;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.assistant.persistence.AssistenteToolRepository;
import com.agentapi.exception.AssistantNotFoundException;

@Service
@Transactional(readOnly = true)
public class AssistantToolBindingService {

    private final AssistenteRepository assistenteRepository;
    private final AssistenteToolRepository assistenteToolRepository;

    public AssistantToolBindingService(
            AssistenteRepository assistenteRepository,
            AssistenteToolRepository assistenteToolRepository) {
        this.assistenteRepository = assistenteRepository;
        this.assistenteToolRepository = assistenteToolRepository;
    }

    public List<String> toolNamesForAssistant(String assistantCode) {
        UUID id = assistenteRepository
                .findByCdAssistenteIgnoreCase(AssistantCodes.normalize(assistantCode))
                .orElseThrow(() -> new AssistantNotFoundException(assistantCode))
                .getIdAssistente();
        return assistenteToolRepository.findByIdAssistenteOrderByNmToolAsc(id).stream()
                .map(t -> t.getNmTool())
                .toList();
    }
}
