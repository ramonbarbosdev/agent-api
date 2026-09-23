package com.agentapi.rag;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {

    private TextChunker() {
    }

    public static List<String> split(String text, int maxChars, int overlapChars) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normalized = text.trim().replace("\r\n", "\n");
        if (normalized.length() <= maxChars) {
            return List.of(normalized);
        }

        int overlap = Math.max(0, Math.min(overlapChars, maxChars / 2));
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(normalized.length(), start + maxChars);
            if (end < normalized.length()) {
                int breakAt = normalized.lastIndexOf('\n', end);
                if (breakAt > start + maxChars / 3) {
                    end = breakAt;
                }
            }
            String piece = normalized.substring(start, end).trim();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
            if (end >= normalized.length()) {
                break;
            }
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }
}
