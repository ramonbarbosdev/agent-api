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
        sb.append("Trechos ja recuperados da base indexada para a pergunta do usuario. ");
        sb.append("Responda em portugues natural usando SOMENTE o texto abaixo — nao diga 'consulte a base' nem cite ferramentas. ");
        sb.append("Mencione o titulo do documento quando usar um trecho. ");
        sb.append("Se nada abaixo responder, diga que nao encontrou na base.\n");
        int index = 1;
        for (RagHit hit : hits) {
            sb.append("\n[").append(index++).append("] ");
            sb.append("Titulo: ").append(hit.titulo()).append(" | Fonte: ").append(hit.fonte()).append("\n");
            sb.append(hit.conteudo().trim()).append("\n");
        }
        return sb.toString().trim();
    }
}
