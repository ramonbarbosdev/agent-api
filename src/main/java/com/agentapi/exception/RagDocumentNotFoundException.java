package com.agentapi.exception;

import java.util.UUID;

public class RagDocumentNotFoundException extends ApiException {

    public RagDocumentNotFoundException(UUID id) {
        super(ErrorCode.RAG_DOCUMENT_NOT_FOUND, "Documento não encontrado: " + id);
    }
}
