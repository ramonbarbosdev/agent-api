package com.agentapi.agent.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.agentapi.web.AgentChatHistoryMessage;

@Component
public class ContextWindowTrimmer {

    public TrimResult trimHistory(List<AgentChatHistoryMessage> history, int maxContextChars, String systemPrompt, String userMessage) {
        if (history == null || history.isEmpty()) {
            return new TrimResult(List.of(), 0);
        }

        int reserved = estimateChars(systemPrompt) + estimateChars(userMessage) + 256;
        int budget = Math.max(0, maxContextChars - reserved);

        List<AgentChatHistoryMessage> working = new ArrayList<>(history);
        int dropped = 0;

        while (!working.isEmpty() && historyCharCount(working) > budget) {
            working.remove(0);
            dropped++;
        }

        return new TrimResult(List.copyOf(working), dropped);
    }

    private static int historyCharCount(List<AgentChatHistoryMessage> history) {
        return history.stream()
                .mapToInt(item -> estimateChars(item.getRole()) + estimateChars(item.getContent()) + 8)
                .sum();
    }

    private static int estimateChars(String text) {
        return text == null ? 0 : text.length();
    }

    public record TrimResult(List<AgentChatHistoryMessage> history, int droppedCount) {
    }
}
