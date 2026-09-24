package com.agentapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(ErrorCode.INVALID_REQUEST, "Requisição inválida."));
    }

    @ExceptionHandler(AssistantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssistantNotFound(AssistantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(RagDocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRagDocumentNotFound(RagDocumentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(LlmException.class)
    public ResponseEntity<ErrorResponse> handleLlm(LlmException ex) {
        HttpStatus status = mapLlmStatus(ex.getCode());
        if (status.is5xxServerError()) {
            log.error("LLM error [{}]: {}", ex.getCode(), ex.getMessage(), ex);
        } else {
            log.warn("LLM error [{}]: {}", ex.getCode(), ex.getMessage());
        }
        return ResponseEntity.status(status)
                .body(ErrorResponse.from(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        HttpStatus status = ex instanceof ToolException ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ErrorResponse.from(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.from(ErrorCode.INTERNAL_ERROR,
                        "Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    private static HttpStatus mapLlmStatus(ErrorCode code) {
        return switch (code) {
            case MODEL_NOT_FOUND -> HttpStatus.BAD_GATEWAY;
            case LLM_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case LLM_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case LLM_COMMUNICATION_ERROR -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
