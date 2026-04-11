package com.monew.domain.article.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleDto(
    UUID id,
    String source,
    String sourceUrl,
    String title,
    String summary,
    Instant publishedAt,
    UUID interestId,
    String interestName,
    long viewCount,
    long commentCount,
    boolean viewedByMe
) {

}
