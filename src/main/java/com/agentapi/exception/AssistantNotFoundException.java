package com.agentapi.exception;

public class AssistantNotFoundException extends ApiException {

    public AssistantNotFoundException(String assistantKey) {
        super(ErrorCode.ASSISTANT_NOT_FOUND,
                "Assistente não encontrado: " + assistantKey);
    }
}
