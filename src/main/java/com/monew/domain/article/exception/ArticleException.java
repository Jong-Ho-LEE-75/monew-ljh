package com.monew.domain.article.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class ArticleException extends MonewException {

    public ArticleException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
