package com.agentapi.web;

public record RagDocumentSummaryDto(
        String documentoId,
        String titulo,
        String fonte,
        int chunkCount,
        String criadoEm) {
}
