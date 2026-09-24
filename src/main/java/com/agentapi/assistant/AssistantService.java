package com.agentapi.assistant;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.exception.AssistantNotFoundException;

@Service
@Transactional(readOnly = true)
public class AssistantService {

    private final AssistenteRepository assistenteRepository;
    private final AssistantMapper assistantMapper;

    public AssistantService(AssistenteRepository assistenteRepository, AssistantMapper assistantMapper) {
        this.assistenteRepository = assistenteRepository;
        this.assistantMapper = assistantMapper;
    }

    public Optional<Assistant> findActiveByCode(String code) {
        return assistenteRepository.findByCdAssistenteIgnoreCase(AssistantCodes.normalize(code))
                .filter(AssistenteEntity::isFlAtivo)
                .map(assistantMapper::toDomain);
    }

    public Assistant requireActiveByCode(String code) {
        return findActiveByCode(code).orElseThrow(() -> new AssistantNotFoundException(code));
    }

    public Optional<Assistant> findByCode(String code) {
        return assistenteRepository.findByCdAssistenteIgnoreCase(AssistantCodes.normalize(code))
                .map(assistantMapper::toDomain);
    }

    public List<Assistant> listActive() {
        return assistenteRepository.findByFlAtivoTrueOrderByNmNomeAsc().stream()
                .map(assistantMapper::toDomain)
                .toList();
    }

    public List<Assistant> listAll() {
        return assistenteRepository.findAllByOrderByNmNomeAsc().stream()
                .map(assistantMapper::toDomain)
                .toList();
    }
}
