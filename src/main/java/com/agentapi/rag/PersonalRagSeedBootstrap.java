package com.agentapi.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.rag.persistence.DocumentoRepository;

/**
 * Garante documentos RAG iniciais do assistente pessoal (classpath), sem duplicar por título.
 */
@Component
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PersonalRagSeedBootstrap {

    private static final Logger log = LoggerFactory.getLogger(PersonalRagSeedBootstrap.class);

    private static final String FONTE_PREFIX = "pessoal/seed";

    private record SeedSpec(String titulo, String fonte, String classpathFile) {
    }

    private static final List<SeedSpec> SEEDS = List.of(
            new SeedSpec(
                    "Sobre mim — assistente pessoal",
                    FONTE_PREFIX + "/sobre-mim",
                    "rag-seeds/personal-sobre-mim.md"),
            new SeedSpec(
                    "Projetos e prioridades",
                    FONTE_PREFIX + "/projetos",
                    "rag-seeds/personal-projetos.md"));

    private final DocumentoRepository documentoRepository;
    private final RagIngestionService ingestionService;

    public PersonalRagSeedBootstrap(
            DocumentoRepository documentoRepository,
            RagIngestionService ingestionService) {
        this.documentoRepository = documentoRepository;
        this.ingestionService = ingestionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(110)
    @Transactional
    public void seedPersonalDocumentsIfMissing() {
        for (SeedSpec spec : SEEDS) {
            if (documentoRepository.existsByNmTituloIgnoreCase(spec.titulo())) {
                continue;
            }
            String conteudo = loadClasspath(spec.classpathFile());
            if (conteudo.isBlank()) {
                log.warn("Seed RAG vazio: {}", spec.classpathFile());
                continue;
            }
            UUID id = ingestionService.ingest(spec.titulo(), spec.fonte(), conteudo);
            log.info("Documento RAG pessoal indexado (id={}, titulo={})", id, spec.titulo());
        }
    }

    private static String loadClasspath(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return "";
            }
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.warn("Falha ao ler seed RAG {}: {}", path, ex.getMessage());
            return "";
        }
    }
}
