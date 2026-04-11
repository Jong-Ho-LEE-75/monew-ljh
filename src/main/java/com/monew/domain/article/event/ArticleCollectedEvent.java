package com.monew.domain.article.event;

import java.util.UUID;

public record ArticleCollectedEvent(
    UUID articleId,
    UUID interestId,
    String interestName,
    String articleTitle
) {

}
