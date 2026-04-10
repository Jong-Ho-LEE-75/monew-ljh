package com.monew.domain.comment.exception;

import com.monew.common.exception.ErrorCode;
import com.monew.common.exception.MonewException;
import java.util.Map;

public class CommentException extends MonewException {

    public CommentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
