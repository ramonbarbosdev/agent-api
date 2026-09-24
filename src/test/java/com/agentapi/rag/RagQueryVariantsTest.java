package com.agentapi.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RagQueryVariantsTest {

    @Test
    void extractsReferenceCodeFromLongQuestion() {
        String q = "Qual o código LAB-RAG-2026 e quantos dias úteis para revisão voluntária de benefícios?";
        assertThat(RagQueryVariants.fallbacks(q)).contains("LAB-RAG-2026");
    }

    @Test
    void chatRetrievalExpandsObjectivesQuestion() {
        assertThat(RagQueryVariants.chatRetrievalQueries("Quais são meus objetivos agora?"))
                .contains("objetivos prioridades semana projetos");
    }
}
