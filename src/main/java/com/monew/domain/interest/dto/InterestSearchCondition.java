package com.monew.domain.interest.dto;

import com.monew.domain.article.dto.SortDirection;

public record InterestSearchCondition(
    String keyword,
    InterestSortBy sortBy,
    SortDirection direction
) {
    public InterestSortBy sortByOrDefault() {
        return sortBy == null ? InterestSortBy.NAME : sortBy;
    }

    public SortDirection directionOrDefault() {
        return direction == null ? SortDirection.ASC : direction;
    }
}
