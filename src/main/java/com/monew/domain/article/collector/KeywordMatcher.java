package com.monew.domain.article.collector;

import java.util.List;

public final class KeywordMatcher {

    private KeywordMatcher() {
    }

    public static boolean matches(CollectedArticle article, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        String haystack = buildHaystack(article);
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            if (haystack.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String buildHaystack(CollectedArticle article) {
        StringBuilder sb = new StringBuilder();
        if (article.title() != null) {
            sb.append(article.title().toLowerCase()).append(' ');
        }
        if (article.summary() != null) {
            sb.append(article.summary().toLowerCase());
        }
        return sb.toString();
    }
}
