package com.agentapi.rag;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.RagDocumentDetailDto;
import com.agentapi.web.RagDocumentRequest;
import com.agentapi.web.RagDocumentResponse;
import com.agentapi.web.RagDocumentSummaryDto;
import com.agentapi.web.RagSearchHitDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent/rag")
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RagController {

    private final RagIngestionService ingestionService;
    private final RagSearchService searchService;
    private final RagDocumentService documentService;

    public RagController(
            RagIngestionService ingestionService,
            RagSearchService searchService,
            RagDocumentService documentService) {
        this.ingestionService = ingestionService;
        this.searchService = searchService;
        this.documentService = documentService;
    }

    @GetMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RagDocumentSummaryDto> listDocuments() {
        return documentService.listDocuments();
    }

    @GetMapping(value = "/documents/{documentoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RagDocumentDetailDto getDocument(@PathVariable UUID documentoId) {
        return documentService.getDocument(documentoId);
    }

    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagDocumentResponse ingest(@Valid @RequestBody RagDocumentRequest request) {
        var id = ingestionService.ingest(request.getTitulo(), request.getFonte(), request.getConteudo());
        return new RagDocumentResponse(id.toString());
    }

    @PutMapping(value = "/documents/{documentoId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagDocumentDetailDto updateDocument(
            @PathVariable UUID documentoId,
            @Valid @RequestBody RagDocumentRequest request) {
        return documentService.updateDocument(
                documentoId,
                request.getTitulo(),
                request.getFonte(),
                request.getConteudo());
    }

    @DeleteMapping(value = "/documents/{documentoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable UUID documentoId) {
        documentService.deleteDocument(documentoId);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RagSearchHitDto> search(@RequestParam String q) {
        return searchService.search(q).stream()
                .map(hit -> new RagSearchHitDto(hit.titulo(), hit.fonte(), hit.conteudo(), hit.score()))
                .toList();
    }
}
