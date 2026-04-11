package com.monew.domain.comment.mapper;

import com.monew.domain.comment.dto.CommentDto;
import com.monew.domain.comment.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment, boolean likedByMe) {
        return new CommentDto(
            comment.getId(),
            comment.getArticle().getId(),
            comment.getUser().getId(),
            comment.getUser().getNickname(),
            comment.getContent(),
            comment.getLikeCount(),
            likedByMe,
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
