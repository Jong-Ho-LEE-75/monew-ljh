package com.monew.domain.article.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleSearchCondition(
    String keyword,
    List<String> sourceIn,
    Instant publishedFrom,
    Instant publishedTo,
    UUID interestId,
    ArticleSortBy sortBy,
    SortDirection direction
) {

    public SortDirection directionOrDefault() {
        return direction == null ? SortDirection.DESC : direction;
    }

    public ArticleSortBy sortByOrDefault() {
        return sortBy == null ? ArticleSortBy.PUBLISHED_AT : sortBy;
    }
}
