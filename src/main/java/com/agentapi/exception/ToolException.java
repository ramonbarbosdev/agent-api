package com.agentapi.exception;

public class ToolException extends ApiException {

    public ToolException(ErrorCode code, String message) {
        super(code, message);
    }
}
