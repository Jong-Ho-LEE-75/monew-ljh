package com.monew.domain.comment.exception;

import com.monew.common.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class CommentNotOwnedException extends CommentException {

    public CommentNotOwnedException(UUID commentId, UUID userId) {
        super(ErrorCode.COMMENT_NOT_OWNED, Map.of("commentId", commentId, "userId", userId));
    }
}
