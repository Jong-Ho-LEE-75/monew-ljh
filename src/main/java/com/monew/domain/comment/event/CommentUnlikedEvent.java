package com.monew.domain.comment.event;

import java.util.UUID;

public record CommentUnlikedEvent(
    UUID commentId,
    UUID commentOwnerId,
    UUID likerId,
    long commentLikeCount
) {

}
