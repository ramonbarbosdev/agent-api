package com.agentapi.rag;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.config.RagProperties;
import com.agentapi.rag.persistence.DocumentoChunkEntity;
import com.agentapi.rag.persistence.DocumentoChunkRepository;
import com.agentapi.rag.persistence.DocumentoEntity;
import com.agentapi.rag.persistence.DocumentoRepository;

@Service
public class RagIngestionService {

    private final RagProperties ragProperties;
    private final DocumentoRepository documentoRepository;
    private final DocumentoChunkRepository chunkRepository;

    public RagIngestionService(
            RagProperties ragProperties,
            DocumentoRepository documentoRepository,
            DocumentoChunkRepository chunkRepository) {
        this.ragProperties = ragProperties;
        this.documentoRepository = documentoRepository;
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public UUID ingest(String titulo, String fonte, String conteudo) {
        UUID documentoId = UUID.randomUUID();
        documentoRepository.save(new DocumentoEntity(documentoId, titulo, fonte, conteudo));
        indexChunks(documentoId, conteudo);
        return documentoId;
    }

    @Transactional
    public void reindexDocument(UUID documentoId, String conteudo) {
        chunkRepository.deleteByIdDocumento(documentoId);
        indexChunks(documentoId, conteudo);
    }

    void indexChunks(UUID documentoId, String conteudo) {
        List<String> chunks = TextChunker.split(
                conteudo,
                ragProperties.getChunkMaxChars(),
                ragProperties.getChunkOverlapChars());

        int ordem = 1;
        for (String chunk : chunks) {
            chunkRepository.save(new DocumentoChunkEntity(
                    UUID.randomUUID(),
                    documentoId,
                    ordem++,
                    chunk));
        }
    }
}
