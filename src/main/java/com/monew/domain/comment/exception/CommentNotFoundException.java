package com.monew.domain.comment.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends CommentException {

    public CommentNotFoundException(UUID id) {
        super(ErrorCode.COMMENT_NOT_FOUND, Map.of("id", id));
    }
}
