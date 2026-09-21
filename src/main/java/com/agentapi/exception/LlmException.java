package com.agentapi.exception;

public class LlmException extends ApiException {

    private LlmException(ErrorCode code, String message) {
        super(code, message);
    }

    private LlmException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static LlmException of(ErrorCode code, String message) {
        return new LlmException(code, message);
    }

    public static LlmException of(ErrorCode code, String message, Throwable cause) {
        return new LlmException(code, message, cause);
    }
}
