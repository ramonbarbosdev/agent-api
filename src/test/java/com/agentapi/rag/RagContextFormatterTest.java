package com.agentapi.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class RagContextFormatterTest {

    @Test
    void formatsHitsWithSourceLabels() {
        String text = RagContextFormatter.formatForPrompt(List.of(
                new RagHit("Politica HE", "RH 2026", "Aprovacao pelo gestor.", 0.9)));
        assertThat(text).contains("Politica HE").contains("RH 2026").contains("Aprovacao pelo gestor");
    }
}
