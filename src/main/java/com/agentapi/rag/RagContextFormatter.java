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
        sb.append("Use apenas os trechos abaixo para fatos sobre politicas e documentos. ");
        sb.append("Cite a fonte ao responder (ex.: \"fonte: ...\").\n");
        int index = 1;
        for (RagHit hit : hits) {
            sb.append("\n[").append(index++).append("] ");
            sb.append("Titulo: ").append(hit.titulo()).append(" | Fonte: ").append(hit.fonte()).append("\n");
            sb.append(hit.conteudo().trim()).append("\n");
        }
        return sb.toString().trim();
    }
}
