package com.monew.domain.article.collector;

import java.time.Instant;

public record CollectedArticle(
    String source,
    String sourceUrl,
    String title,
    String summary,
    Instant publishedAt
) {

}
