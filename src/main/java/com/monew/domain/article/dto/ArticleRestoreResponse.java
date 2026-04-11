package com.monew.domain.article.dto;

import java.time.LocalDate;

public record ArticleRestoreResponse(LocalDate date, int restored) {
}
