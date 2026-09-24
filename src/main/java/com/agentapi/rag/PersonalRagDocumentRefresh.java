package com.agentapi.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.rag.persistence.DocumentoEntity;
import com.agentapi.rag.persistence.DocumentoRepository;

/**
 * Reindexa seeds pessoais do classpath quando o arquivo mudou (hash do conteúdo), para dev sem duplicar título.
 */
@Component
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PersonalRagDocumentRefresh {

    private static final Logger log = LoggerFactory.getLogger(PersonalRagDocumentRefresh.class);

    private record SeedSpec(String titulo, String fonte, String classpathFile) {
    }

    private static final List<SeedSpec> SEEDS = List.of(
            new SeedSpec(
                    "Sobre mim — assistente pessoal",
                    "pessoal/seed/sobre-mim",
                    "rag-seeds/personal-sobre-mim.md"),
            new SeedSpec(
                    "Projetos e prioridades",
                    "pessoal/seed/projetos",
                    "rag-seeds/personal-projetos.md"));

    private final DocumentoRepository documentoRepository;
    private final RagIngestionService ingestionService;

    public PersonalRagDocumentRefresh(
            DocumentoRepository documentoRepository,
            RagIngestionService ingestionService) {
        this.documentoRepository = documentoRepository;
        this.ingestionService = ingestionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(111)
    @Transactional
    public void refreshSeedDocumentsWhenContentChanged() {
        for (SeedSpec spec : SEEDS) {
            String conteudo = loadClasspath(spec.classpathFile());
            if (conteudo.isBlank()) {
                continue;
            }
            documentoRepository.findByNmTituloIgnoreCase(spec.titulo()).ifPresentOrElse(
                    doc -> {
                        if (conteudo.equals(doc.getDsConteudo())) {
                            return;
                        }
                        doc.setDsConteudo(conteudo);
                        doc.setNmFonte(spec.fonte());
                        documentoRepository.save(doc);
                        ingestionService.reindexDocument(doc.getIdDocumento(), conteudo);
                        log.info("Documento RAG pessoal reindexado (titulo={})", spec.titulo());
                    },
                    () -> {
                        // criado por PersonalRagSeedBootstrap se ausente
                    });
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
            log.warn("Falha ao ler {}: {}", path, ex.getMessage());
            return "";
        }
    }
}
