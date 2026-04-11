package com.monew.domain.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    long likeCount,
    boolean likedByMe,
    Instant createdAt,
    Instant updatedAt
) {

}
