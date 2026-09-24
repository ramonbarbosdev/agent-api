package com.agentapi.web;

public record RagDocumentDetailDto(
        String documentoId,
        String titulo,
        String fonte,
        String conteudo,
        int chunkCount,
        String criadoEm) {
}
