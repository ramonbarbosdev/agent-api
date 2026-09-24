package com.agentapi.rag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gera consultas alternativas quando a pergunta completa do usuário não casa bem com FTS (AND de todos os termos).
 */
final class RagQueryVariants {

    private static final Pattern REFERENCE_CODE = Pattern.compile("\\b[A-Za-z0-9]{2,}(?:-[A-Za-z0-9]+)+\\b");

    private RagQueryVariants() {
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

    private static String significantTerms(String query, int maxTerms) {
        String[] words = query.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String word : words) {
            String clean = word.replaceAll("[^\\p{L}\\p{N}-]", "");
            if (clean.length() < 5) {
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
