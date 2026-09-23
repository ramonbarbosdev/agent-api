package com.agentapi.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextChunkerTest {

    @Test
    void splitsLongTextIntoOverlappingChunks() {
        String text = "a".repeat(500) + "\n" + "b".repeat(500);
        var chunks = TextChunker.split(text, 400, 50);
        assertThat(chunks.size()).isGreaterThan(1);
        assertThat(chunks.get(0).length()).isLessThanOrEqualTo(400);
    }
}
