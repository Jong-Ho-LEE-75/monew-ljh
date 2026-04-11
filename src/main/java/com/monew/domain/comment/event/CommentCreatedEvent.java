package com.monew.domain.comment.event;

import java.time.Instant;
import java.util.UUID;

public record CommentCreatedEvent(
    UUID commentId,
    UUID articleId,
    String articleTitle,
    UUID userId,
    String userNickname,
    String content,
    Instant createdAt
) {

}
