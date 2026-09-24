package com.agentapi.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gera consultas alternativas quando a pergunta completa do usuário não casa bem com FTS (AND de todos os termos).
 */
final class RagQueryVariants {

    private static final Pattern REFERENCE_CODE = Pattern.compile("\\b[A-Za-z0-9]{2,}(?:-[A-Za-z0-9]+)+\\b");

    private static final Set<String> STOPWORDS = Set.of(
            "quais", "qual", "como", "quando", "onde", "quem", "que", "sao", "são", "ser", "esta", "está",
            "meus", "minhas", "meu", "minha", "seus", "suas", "agora", "hoje", "voce", "você", "para", "com",
            "sem", "nao", "não", "sim", "uma", "um", "uns", "umas", "nos", "nas", "aos", "às",
            "the", "and", "or", "de", "da", "do", "dos", "das", "em", "no", "na", "por", "sobre");

    private RagQueryVariants() {
    }

    /**
     * Consultas em ordem de prioridade para recuperar contexto no chat (pergunta natural do usuário).
     */
    static List<String> chatRetrievalQueries(String rawUserMessage) {
        if (rawUserMessage == null || rawUserMessage.isBlank()) {
            return List.of();
        }
        String normalized = rawUserMessage.trim().toLowerCase(Locale.ROOT);
        LinkedHashSet<String> ordered = new LinkedHashSet<>();

        if (containsAny(normalized, "objetiv", "prioridad", "meta ", "metas", "foco")) {
            ordered.add("objetivos prioridades semana projetos");
            ordered.add("objetivos");
            ordered.add("prioridade");
        }
        if (containsAny(normalized, "projeto", "agent-api", "agent-front", "agent api")) {
            ordered.add("agent-api agent-front projetos");
        }
        if (containsAny(normalized, "rotina", "preferenc", "sobre mim", "quem sou")) {
            ordered.add("rotina preferencias objetivos");
        }

        String keywords = keywordLine(normalized);
        if (!keywords.isBlank()) {
            ordered.add(keywords);
        }
        ordered.add(normalized);
        return List.copyOf(ordered);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    static List<String> fallbacks(String normalizedQuery) {
        Set<String> variants = new LinkedHashSet<>();
        Matcher matcher = REFERENCE_CODE.matcher(normalizedQuery);
        while (matcher.find()) {
            variants.add(matcher.group());
        }
        String significant = significantTerms(normalizedQuery, 6);
        if (!significant.isBlank()) {
            variants.add(significant);
        }
        variants.remove(normalizedQuery);
        return List.copyOf(variants);
    }

    private static String keywordLine(String query) {
        String[] words = query.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            String clean = word.replaceAll("[^\\p{L}\\p{N}-]", "").toLowerCase(Locale.ROOT);
            if (clean.length() < 4 || STOPWORDS.contains(clean)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(clean);
        }
        return sb.toString().trim();
    }

    private static String significantTerms(String query, int maxTerms) {
        String keywords = keywordLine(query);
        if (!keywords.isBlank()) {
            return keywords;
        }
        String[] words = query.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String word : words) {
            String clean = word.replaceAll("[^\\p{L}\\p{N}-]", "");
            if (clean.length() < 4) {
                continue;
            }
            if (count >= maxTerms) {
                break;
            }
            if (count > 0) {
                sb.append(' ');
            }
            sb.append(clean);
            count++;
        }
        return sb.toString().trim();
    }
}
