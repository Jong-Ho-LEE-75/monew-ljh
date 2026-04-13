package com.monew.domain.comment.event;

import java.time.Instant;
import java.util.UUID;

public record CommentLikedEvent(
    UUID commentId,
    UUID commentOwnerId,
    UUID likerId,
    String likerNickname,
    UUID articleId,
    String articleTitle,
    String commentContent,
    Instant commentCreatedAt
) {

}
