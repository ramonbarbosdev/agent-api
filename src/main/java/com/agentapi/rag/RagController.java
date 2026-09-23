package com.agentapi.rag;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.RagDocumentRequest;
import com.agentapi.web.RagDocumentResponse;
import com.agentapi.web.RagSearchHitDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent/rag")
@ConditionalOnProperty(prefix = "agent.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RagController {

    private final RagIngestionService ingestionService;
    private final RagSearchService searchService;

    public RagController(RagIngestionService ingestionService, RagSearchService searchService) {
        this.ingestionService = ingestionService;
        this.searchService = searchService;
    }

    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagDocumentResponse ingest(@Valid @RequestBody RagDocumentRequest request) {
        var id = ingestionService.ingest(request.getTitulo(), request.getFonte(), request.getConteudo());
        return new RagDocumentResponse(id.toString());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RagSearchHitDto> search(@RequestParam String q) {
        return searchService.search(q).stream()
                .map(hit -> new RagSearchHitDto(hit.titulo(), hit.fonte(), hit.conteudo(), hit.score()))
                .toList();
    }
}
