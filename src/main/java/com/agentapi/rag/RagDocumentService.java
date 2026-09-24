package com.agentapi.rag;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agentapi.exception.RagDocumentNotFoundException;
import com.agentapi.rag.persistence.DocumentoChunkRepository;
import com.agentapi.rag.persistence.DocumentoEntity;
import com.agentapi.rag.persistence.DocumentoRepository;
import com.agentapi.web.RagDocumentDetailDto;
import com.agentapi.web.RagDocumentSummaryDto;

@Service
public class RagDocumentService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DocumentoRepository documentoRepository;
    private final DocumentoChunkRepository chunkRepository;
    private final RagIngestionService ingestionService;

    public RagDocumentService(
            DocumentoRepository documentoRepository,
            DocumentoChunkRepository chunkRepository,
            RagIngestionService ingestionService) {
        this.documentoRepository = documentoRepository;
        this.chunkRepository = chunkRepository;
        this.ingestionService = ingestionService;
    }

    @Transactional(readOnly = true)
    public List<RagDocumentSummaryDto> listDocuments() {
        return documentoRepository.findAll(Sort.by(Sort.Direction.DESC, "dtCriacao")).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RagDocumentDetailDto getDocument(UUID id) {
        return toDetail(requireDocument(id));
    }

    @Transactional
    public RagDocumentDetailDto updateDocument(UUID id, String titulo, String fonte, String conteudo) {
        DocumentoEntity doc = requireDocument(id);
        doc.setNmTitulo(titulo);
        doc.setNmFonte(fonte);
        doc.setDsConteudo(conteudo);
        documentoRepository.save(doc);
        ingestionService.reindexDocument(id, conteudo);
        return toDetail(doc);
    }

    @Transactional
    public void deleteDocument(UUID id) {
        if (!documentoRepository.existsById(id)) {
            throw new RagDocumentNotFoundException(id);
        }
        documentoRepository.deleteById(id);
    }

    private DocumentoEntity requireDocument(UUID id) {
        return documentoRepository.findById(id).orElseThrow(() -> new RagDocumentNotFoundException(id));
    }

    private RagDocumentSummaryDto toSummary(DocumentoEntity doc) {
        int chunks = (int) chunkRepository.countByIdDocumento(doc.getIdDocumento());
        String criadoEm = doc.getDtCriacao() != null ? ISO.format(doc.getDtCriacao()) : "";
        return new RagDocumentSummaryDto(
                doc.getIdDocumento().toString(),
                doc.getNmTitulo(),
                doc.getNmFonte(),
                chunks,
                criadoEm);
    }

    private RagDocumentDetailDto toDetail(DocumentoEntity doc) {
        int chunks = (int) chunkRepository.countByIdDocumento(doc.getIdDocumento());
        String criadoEm = doc.getDtCriacao() != null ? ISO.format(doc.getDtCriacao()) : "";
        return new RagDocumentDetailDto(
                doc.getIdDocumento().toString(),
                doc.getNmTitulo(),
                doc.getNmFonte(),
                doc.getDsConteudo(),
                chunks,
                criadoEm);
    }
}
