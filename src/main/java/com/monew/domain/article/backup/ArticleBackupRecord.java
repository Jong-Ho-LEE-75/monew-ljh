package com.monew.domain.article.backup;

import java.time.Instant;
import java.util.UUID;

public record ArticleBackupRecord(
    UUID id,
    String source,
    String sourceUrl,
    String title,
    String summary,
    Instant publishedAt,
    UUID interestId,
    Instant createdAt
) {

}
