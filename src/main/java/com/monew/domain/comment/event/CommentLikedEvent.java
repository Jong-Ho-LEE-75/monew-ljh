package com.monew.domain.comment.event;

import java.util.UUID;

public record CommentLikedEvent(
    UUID commentId,
    UUID commentOwnerId,
    UUID likerId,
    String likerNickname
) {

}
