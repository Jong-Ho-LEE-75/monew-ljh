package com.monew.domain.article.event;

import java.time.Instant;
import java.util.UUID;

public record ArticleViewedEvent(
    UUID articleViewId,
    UUID articleId,
    UUID userId,
    String source,
    String sourceUrl,
    String articleTitle,
    String articleSummary,
    Instant articlePublishedAt,
    long articleViewCount,
    long articleCommentCount,
    Instant viewedAt
) {

}
