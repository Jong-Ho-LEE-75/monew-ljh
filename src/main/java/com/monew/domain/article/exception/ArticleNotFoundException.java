package com.monew.domain.article.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ArticleNotFoundException extends ArticleException {

    public ArticleNotFoundException(UUID articleId) {
        super(ErrorCode.ARTICLE_NOT_FOUND, Map.of("articleId", articleId));
    }
}
