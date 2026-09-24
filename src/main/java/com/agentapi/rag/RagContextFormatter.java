package com.agentapi.rag;

import java.util.List;

public final class RagContextFormatter {

    private RagContextFormatter() {
    }

    public static String formatForPrompt(List<RagHit> hits) {
        if (hits == null || hits.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Estes trechos JA foram buscados na base indexada; nao e necessario chamar ferramenta de busca. ");
        sb.append("Responda usando SOMENTE o texto abaixo para fatos, prazos e codigos. ");
        sb.append("Reproduza numeros literalmente (ex.: 17 dias uteis, nao outro valor). ");
        sb.append("Se nenhum trecho abaixo responder a pergunta, diga que nao encontrou na base.\n");
        int index = 1;
        for (RagHit hit : hits) {
            sb.append("\n[").append(index++).append("] ");
            sb.append("Titulo: ").append(hit.titulo()).append(" | Fonte: ").append(hit.fonte()).append("\n");
            sb.append(hit.conteudo().trim()).append("\n");
        }
        return sb.toString().trim();
    }
}
