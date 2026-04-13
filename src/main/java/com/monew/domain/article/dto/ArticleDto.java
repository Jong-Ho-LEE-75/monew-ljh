package com.monew.domain.article.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record ArticleDto(
    UUID id,
    String source,
    String sourceUrl,
    String title,
    String summary,
    @JsonProperty("publishDate") Instant publishedAt,
    UUID interestId,
    String interestName,
    long viewCount,
    long commentCount,
    boolean viewedByMe
) {

}
