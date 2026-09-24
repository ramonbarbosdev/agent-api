package com.agentapi.llm;

import java.util.function.Consumer;

public interface LlmClient {

    LlmResponse chat(LlmRequest request);

    /**
     * Streaming quando o provider suporta; caso contrário emite o conteúdo completo de uma vez.
     */
    default LlmResponse chatStream(LlmRequest request, Consumer<String> onToken) {
        LlmResponse response = chat(request);
        String content = response.content();
        if (content != null && !content.isEmpty()) {
            onToken.accept(content);
        }
        return response;
    }
}
