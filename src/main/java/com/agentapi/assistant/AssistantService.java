package com.agentapi.assistant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.assistant.persistence.AssistenteEntity;
import com.agentapi.assistant.persistence.AssistenteRepository;
import com.agentapi.auth.CurrentUser;
import com.agentapi.exception.AssistantNotFoundException;

@Service
@Transactional(readOnly = true)
public class AssistantService {

    private final AssistenteRepository assistenteRepository;
    private final AssistantMapper assistantMapper;
    private final CurrentUser currentUser;

    public AssistantService(
            AssistenteRepository assistenteRepository,
            AssistantMapper assistantMapper,
            CurrentUser currentUser) {
        this.assistenteRepository = assistenteRepository;
        this.assistantMapper = assistantMapper;
        this.currentUser = currentUser;
    }

    public Optional<Assistant> findActiveByCode(String code) {
        return assistenteRepository.findByCdAssistenteIgnoreCase(AssistantCodes.normalize(code))
                .filter(AssistenteEntity::isFlAtivo)
                .map(assistantMapper::toDomain);
    }

    public Assistant requireActiveByCode(String code) {
        return findActiveByCode(code).orElseThrow(() -> new AssistantNotFoundException(code));
    }

    public Assistant requireActiveByCodeForUser(String code, UUID userId) {
        return assistenteRepository.findByCdAssistenteIgnoreCaseAndIdUsuario(AssistantCodes.normalize(code), userId)
                .filter(AssistenteEntity::isFlAtivo)
                .map(assistantMapper::toDomain)
                .orElseThrow(() -> new AssistantNotFoundException(code));
    }

    public List<Assistant> listActiveForCurrentUser() {
        UUID userId = currentUser.requireUserId();
        return assistenteRepository.findByIdUsuarioAndFlAtivoTrueOrderByNmNomeAsc(userId).stream()
                .map(assistantMapper::toDomain)
                .toList();
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
