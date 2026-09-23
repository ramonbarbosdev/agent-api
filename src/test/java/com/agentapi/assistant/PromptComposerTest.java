package com.agentapi.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PromptComposerTest {

    private final PromptComposer composer = new PromptComposer();

    @Test
    void composeJoinsBaseAndAssistantPrompts() {
        String result = composer.compose("Base rules", "Domain rules");
        assertThat(result).isEqualTo("Base rules\n\n---\n\nDomain rules");
    }

    @Test
    void composeReturnsAssistantOnlyWhenBaseBlank() {
        assertThat(composer.compose("  ", "Domain")).isEqualTo("Domain");
    }

    @Test
    void composeReturnsBaseOnlyWhenAssistantBlank() {
        assertThat(composer.compose("Base", "")).isEqualTo("Base");
    }
}
